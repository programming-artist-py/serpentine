package penguin.serpentine.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Minimal base config class for Serpentine.
 * Supports defaults, runtime updates, and file path handling.
 */
public abstract class Config {

    private final String modId;
    private Path filePath;

    // Expected keys and defaults
    private final Map<String, Object> expectedDefaults = new LinkedHashMap<>();

    // Current runtime values
    private final Map<String, Object> values = new LinkedHashMap<>();

    public Config(String modId) {
        this.modId = modId;
        expected(); // register expected keys
    }

    /** Called by Serpentine during registration to set the config file path */
    void bindPath(Path configDir) {
        this.filePath = configDir.resolve(modId + ".scnf");
        loadFromFile(); // load values from file (if it exists)
    }

    /** Get the config file path */
    public Path getFilePath() {
        return filePath;
    }

    public String getModId() {
        return modId;
    }

    /** Declare an expected key and default value */
    protected void expect(String key, Object defaultValue) {
        expectedDefaults.put(key, defaultValue);
        // Do not set the value yet — loading will override if file exists
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