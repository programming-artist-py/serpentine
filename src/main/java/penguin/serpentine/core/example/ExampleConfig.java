package penguin.serpentine.core.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import penguin.serpentine.core.Config;

public class ExampleConfig extends Config {

    public float npc_jumppower;
    public boolean enable_snakes;
    public int max_snake_count;
    public String snakeGreeting;

    public float snake_speed;
    public float snake_aggression;
    public boolean allow_venom;
    public int venom_damage;
    public int world_snake_cap;
    public String snake_greeting_style;

    public ExampleConfig(String modId) {
        super(modId);
    }

    @Override
    public void expected() {
        this.expect("npc_jumppower",        5.0F);
        this.expect("enable_snakes",        true);
        this.expect("max_snake_count",      12);
        this.expect("snakeGreeting",        "sssalutations");

        // Add 6 more
        this.expect("snake_speed",          1.25F);
        this.expect("snake_aggression",     0.75F);
        this.expect("allow_venom",          true);
        this.expect("venom_damage",         4);
        this.expect("world_snake_cap",      64);
        this.expect("snake_greeting_style", "hiss");
    }

    @Override
    public void noFileInit() {
        if (Runtime.getRuntime().availableProcessors() > 8) {
            this.max_snake_count = 20;
        }
    }

    @Override
    public void save() {
        Path path = getFilePath();
        StringBuilder sb = new StringBuilder();
        
        getValues().forEach((key, value) -> sb.append(key).append(" = ").append(value).append("\n"));

        try {
            Files.writeString(path, sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to save config: " + path);
        }
    }

    /** update multiple values from a map */
    @Override
    public void updateValues(Map<String, Object> updates) {
        Map<String, Object> updatesCopy = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String key = entry.getKey();
            Object val = entry.getValue();

            // Update fields
            switch (key) {
                case "npc_jumppower"        -> npc_jumppower        = Float.parseFloat(val.toString());
                case "enable_snakes"        -> enable_snakes        = Boolean.parseBoolean(val.toString());
                case "max_snake_count"      -> max_snake_count      = Integer.parseInt(val.toString());
                case "snakeGreeting"        -> snakeGreeting        = val.toString();

                case "snake_speed"          -> snake_speed          = Float.parseFloat(val.toString());
                case "snake_aggression"     -> snake_aggression     = Float.parseFloat(val.toString());
                case "allow_venom"          -> allow_venom          = Boolean.parseBoolean(val.toString());
                case "venom_damage"         -> venom_damage         = Integer.parseInt(val.toString());
                case "world_snake_cap"      -> world_snake_cap      = Integer.parseInt(val.toString());
                case "snake_greeting_style" -> snake_greeting_style = val.toString();

                default -> throw new IllegalArgumentException("Unknown key: " + key);
            }

            // Add to copy to update Config's internal map
            updatesCopy.put(key, val);
        }

        // Update the internal map and save
        super.updateValues(updatesCopy);
    }

    @Override
    public void afterLoad() {
        if (npc_jumppower < 0.0F) npc_jumppower = 0.0F;
        if (max_snake_count < 0)  max_snake_count = 0;

        if (snake_speed < 0.0F)       snake_speed = 0.0F;
        if (snake_aggression < 0.0F)  snake_aggression = 0.0F;
        if (venom_damage < 0)         venom_damage = 0;
        if (world_snake_cap < 0)      world_snake_cap = 0;
    }
}