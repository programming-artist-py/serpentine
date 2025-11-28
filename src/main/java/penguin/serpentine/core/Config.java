package penguin.serpentine.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Minimal base config class for Serpentine.
 * Supports defaults, runtime updates, and file path handling.
 */
public abstract class Config {

    private final String modId;
    private Path filePath;

    // Expected keys and defaults
    protected final Map<String, Object> expectedDefaults = new LinkedHashMap<>();

    // Current runtime values
    protected final Map<String, Object> values = new LinkedHashMap<>();

    protected Map<String, SyncSide> syncSide = new HashMap<>();

    private final Map<String, Object> serverValues = new LinkedHashMap<>();

    public enum SyncSide {
        SERVER_ONLY,
        CLIENT_ONLY,
        SYNCED
    }

    public Config(String modId) {
        this.modId = modId;
        expected(); // register expected keys
    }

    /** Called by Serpentine during registration to set the config file path */
    void bindPath(Path configDir, boolean isServer) {
        if (isServer) {
            Path serverDir = configDir.resolve("server");
            try {
                Files.createDirectories(serverDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.filePath = serverDir.resolve(modId + ".scnf");
        } else {
            this.filePath = configDir.resolve(modId + ".scnf");
        }
        loadFromFile();
    }


    /** Get the config file path */
    public Path getFilePath() {
        return filePath;
    }

    public String getModId() {
        return modId;
    }

    /** Declare an expected key and default value */
    public void expect(String key, Object defaultValue, SyncSide side) {
        expectedDefaults.put(key, defaultValue);
        syncSide.put(key, side);
    }

    /** Get all expected keys and their defaults */
    public Map<String, Object> getExpectedDefaults() {
        return new LinkedHashMap<>(expectedDefaults);
    }

    /** Called by mod to declare all expected keys + defaults */
    public abstract void expected();

    /** Optional: called if file does not exist */
    public void noFileInit() {}

    /** Optional: called after successful load to clamp/validate */
    public void afterLoad() {}

    /** Required: needed to save files */
    public abstract void save();

    /** Update multiple values at once */
    public void updateValues(Map<String, Object> updates) {
        updates.forEach((key, val) -> {
            if (!expectedDefaults.containsKey(key))
                throw new IllegalArgumentException("Unknown key: " + key);
            values.put(key, val);
        });
        save();
    }

    /** Get a copy of all current key->value pairs */
    public Map<String, Object> getValues() {
        return new LinkedHashMap<>(values);
    }

    public Map<String, String> getServerSyncableValues() {
        Map<String, String> out = new HashMap<>();
        for (var entry : values.entrySet()) {
            String key = entry.getKey();
            if (syncSide.get(key) == SyncSide.SYNCED) {
                out.put(key, entry.getValue().toString());
            }
        }
        return out;
    }

    public Map<String, String> getServerOnlyValues() {
        Map<String, String> out = new HashMap<>();
        for (var entry : values.entrySet()) {
            String key = entry.getKey();
            if (syncSide.get(key) == SyncSide.SERVER_ONLY) {
                out.put(key, entry.getValue().toString());
            }
        }
        return out;
    }


    public SyncSide getSyncSide(String key) {
        return syncSide.getOrDefault(key, SyncSide.CLIENT_ONLY);
    }

    public Map<String, Object> getServerValues() {
        return serverValues;
    }

    /** Called on client when server sends a full config sync */
    public void applyServerSync(Map<String, String> syncedValues) {
        serverValues.clear();
        for (var entry : syncedValues.entrySet()) {
            String key = entry.getKey();
            String valueStr = entry.getValue();

            Object defaultVal = expectedDefaults.get(key);
            Object parsedVal;

            if (defaultVal instanceof Boolean) parsedVal = Boolean.parseBoolean(valueStr);
            else if (defaultVal instanceof Integer) parsedVal = Integer.parseInt(valueStr);
            else if (defaultVal instanceof Float) parsedVal = Float.parseFloat(valueStr);
            else parsedVal = valueStr;

            serverValues.put(key, parsedVal);
        }
    }

    public abstract void applyServerEdit(ServerPlayerEntity player, String key, String valueStr);


    public Map<String, String> getValuesAsStrings() {
        Map<String, String> out = new HashMap<>();
        for (var entry : values.entrySet()) {
            Object v = entry.getValue();
            out.put(entry.getKey(), v == null ? "" : v.toString());
        }
        return out;
    }

    /** Load config values from .scnf, fallback to defaults */
    private void loadFromFile() {
        if (filePath == null) return;

        if (Files.exists(filePath)) {
            try (Stream<String> lines = Files.lines(filePath)) {
                lines.forEach(line -> {
                    line = line.strip();
                    if (line.isEmpty() || line.startsWith("#")) return; // skip blank or comments
                    String[] parts = line.split("=", 2);
                    if (parts.length != 2) return;

                    String key = parts[0].strip();
                    String rawValue = parts[1].strip();

                    if (!expectedDefaults.containsKey(key)) return; // ignore unknown keys

                    Object defaultVal = expectedDefaults.get(key);
                    Object parsedValue;

                    if (defaultVal instanceof Boolean) {
                        parsedValue = Boolean.parseBoolean(rawValue);
                    } else if (defaultVal instanceof Integer) {
                        parsedValue = Integer.parseInt(rawValue);
                    } else if (defaultVal instanceof Float) {
                        parsedValue = Float.parseFloat(rawValue);
                    } else if (defaultVal instanceof Double) {
                        parsedValue = Double.parseDouble(rawValue);
                    } else {
                        parsedValue = rawValue;
                    }

                    values.put(key, parsedValue);
                });
            } catch (IOException e) {
                System.err.println("Failed to read config file: " + filePath);
                e.printStackTrace();
            }
        }

        // Fill missing keys with defaults
        expectedDefaults.forEach((key, def) -> values.putIfAbsent(key, def));

        // Run optional post-load validation
        afterLoad();
    }
}