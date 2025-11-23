package penguin.serpentine.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Very small parser for the "key = value" style SCNF format.
 * - skips blank lines and comments starting with "#" or ";"
 * - splits on the first '='
 * - trims key and value
 * - strips surrounding quotes from values
 */
public final class SCNFParser {

    public static Map<String, String> parse(Path path) throws IOException {
        Map<String, String> out = new LinkedHashMap<>();
        if (!Files.exists(path)) return out;
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            if (line.startsWith("#") || line.startsWith(";")) continue;
            // strip inline comments (optional) — split at " #"
            int commentIdx = findInlineComment(line);
            if (commentIdx >= 0) line = line.substring(0, commentIdx).trim();
            int eq = line.indexOf('=');
            if (eq < 0) continue; // ignore malformed lines
            String key = line.substring(0, eq).trim();
            String value = line.substring(eq + 1).trim();
            value = stripQuotes(value);
            value = stripQuotes(value); // ""make sure its not in double double quotes or something""
            out.put(key, value);
        }
        return out;
    }

    private static int findInlineComment(String s) {
        // returns index of first " #" or " ;" or " //" that indicates inline comment
        int idx = s.indexOf(" #");
        if (idx >= 0) return idx;
        idx = s.indexOf(" ;");
        if (idx >= 0) return idx;
        idx = s.indexOf(" //");
        return idx;
    }

    private static String stripQuotes(String v) {
        if (v.length() >= 2) {
            char a = v.charAt(0), b = v.charAt(v.length()-1);
            if ((a == '"' && b == '"') || (a == '\'' && b == '\'')) {
                return v.substring(1, v.length()-1);
            }
        }
        return v;
    }

    public static String render(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            sb.append(e.getKey()).append(" = ").append(valueToString(e.getValue())).append(System.lineSeparator());
        }
        return sb.toString();
    }

    private static String valueToString(Object o) {
        if (o == null) return "";
        if (o instanceof String) {
            String s = (String)o;
            // wrap in quotes if contains spaces or comment chars
            if (s.contains(" ") || s.contains("#") || s.contains(";") || s.contains("=")) {
                return "\"" + s.replace("\"", "\\\"") + "\"";
            } else {
                return s;
            }
        }
        return String.valueOf(o);
    }
}
