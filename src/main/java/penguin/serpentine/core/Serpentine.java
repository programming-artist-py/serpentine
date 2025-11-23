package penguin.serpentine.core;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Registry + loader for configs.
 * Keep it simple: static registry for the library.
 */
public final class Serpentine {
    private static final Map<String, Config> registry = new HashMap<>();
    private static Path configDir;

    /** Call once from your mod initializer (e.g. Fabric's entrypoint) to set config dir. */
    public static void init(Path configDirectory) {
        configDir = configDirectory;
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create config dir: " + configDir, e);
        }
    }

    public static void register(Config cfg) {
        if (configDir == null) {
            // fallback: use working dir /config if not initialized by platform loader
            init(Paths.get("config"));
        }
        if (registry.containsKey(cfg.getModId())) throw new IllegalStateException("Already registered: " + cfg.getModId());
        cfg.bindPath(configDir);
        try {
            loadConfig(cfg);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load config for " + cfg.getModId(), ex);
        }
        registry.put(cfg.getModId(), cfg);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Config> T get(String modId, Class<T> cls) {
        Config c = registry.get(modId);
        if (c == null) throw new IllegalStateException("No config registered for " + modId);
        if (!cls.isInstance(c)) throw new ClassCastException("Config for " + modId + " is not " + cls.getName());
        return (T) c;
    }

    public static void save(Config config) throws IOException {
        if (!registry.containsKey(config.getModId())) {
            throw new IllegalStateException("No config registered for " + config.getModId());
        }
        writeConfig(config);
    }

    public static void saveAll() throws IOException {
        for (Config cfg : registry.values()) writeConfig(cfg);
    }

    // ---- internals ----

    private static void loadConfig(Config cfg) throws IOException {
        cfg.expected(); // fill expected map
        Path file = cfg.getFilePath();
        Map<String, Object> defaultMap = cfg.getExpectedDefaults();
        boolean needsWrite = false;
        if (!Files.exists(file)) {
            // first run: create file from defaults, call noFileInit
            cfg.noFileInit();
            writeFile(file, SCNFParser.render(defaultMap));
            // also set fields on object to defaults
            applyDefaultsToFields(cfg, defaultMap);
            cfg.afterLoad();
            return;
        }
        Map<String, String> parsed = SCNFParser.parse(file);
        Map<String, Object> finalMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : defaultMap.entrySet()) {
            String key = e.getKey();
            Object def = e.getValue();
            if (parsed.containsKey(key)) {
                String raw = parsed.get(key);
                try {
                    Object converted = convertToType(raw, def);
                    finalMap.put(key, converted);
                } catch (Exception ex) {
                    // invalid format for key: write default back and mark for save
                    finalMap.put(key, def);
                    needsWrite = true;
                }
            } else {
                finalMap.put(key, def);
                needsWrite = true;
            }
        }
        // set fields on config object
        applyDefaultsToFields(cfg, finalMap);
        cfg.afterLoad();
        if (needsWrite) {
            writeFile(file, SCNFParser.render(finalMap));
        }
    }

    private static void writeConfig(Config cfg) throws IOException {
        Map<String, Object> map = cfg.getExpectedDefaults();
        // attempt to reflect current field values (so saves reflect runtime changes)
        Map<String, Object> merged = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            String key = e.getKey();
            Object current = readFieldValue(cfg, key);
            merged.put(key, current != null ? current : e.getValue());
        }
        writeFile(cfg.getFilePath(), SCNFParser.render(merged));
    }

    private static void writeFile(Path p, String contents) throws IOException {
        Files.write(p, contents.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static Object readFieldValue(Config cfg, String key) {
        try {
            Field f = cfg.getClass().getField(key);
            return f.get(cfg);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    private static void applyDefaultsToFields(Config cfg, Map<String, Object> values) {
        Class<?> c = cfg.getClass();
        for (Map.Entry<String, Object> e : values.entrySet()) {
            String key = e.getKey();
            Object val = e.getValue();
            try {
                Field f = c.getField(key); // requires public fields
                Class<?> ft = f.getType();
                Object valueToSet = coerceForField(ft, val);
                f.set(cfg, valueToSet);
            } catch (NoSuchFieldException ex) {
                // no matching field — ignore (schema can include keys not represented as fields)
            } catch (IllegalAccessException ex) {
                // should not happen for public fields
            }
        }
    }

    private static Object coerceForField(Class<?> fieldType, Object value) {
        if (value == null) return null;
        if (fieldType.isAssignableFrom(value.getClass())) return value;
        // attempt conversions
        String s = String.valueOf(value);
        if (fieldType == int.class || fieldType == Integer.class) return Integer.parseInt(s);
        if (fieldType == long.class || fieldType == Long.class) return Long.parseLong(s);
        if (fieldType == float.class || fieldType == Float.class) return Float.parseFloat(s);
        if (fieldType == double.class || fieldType == Double.class) return Double.parseDouble(s);
        if (fieldType == boolean.class || fieldType == Boolean.class) return Boolean.parseBoolean(s);
        if (fieldType == String.class) return s;
        // fallback: unsupported, return null
        return null;
    }

    private static Object convertToType(String raw, Object defaultVal) {
        if (defaultVal instanceof Integer) return Integer.parseInt(raw);
        if (defaultVal instanceof Long) return Long.parseLong(raw);
        if (defaultVal instanceof Float) return Float.parseFloat(raw);
        if (defaultVal instanceof Double) return Double.parseDouble(raw);
        if (defaultVal instanceof Boolean) return Boolean.parseBoolean(raw);
        // default to String if default value is String or null
        return raw;
    }

    public static Collection<Config> getAllConfigs() {
        return registry.values();
    }
}