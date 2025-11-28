package penguin.serpentine.screens;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import penguin.serpentine.core.Config;
import penguin.serpentine.core.Config.SyncSide;
import penguin.serpentine.network.ConfigNetworkingClient;
import penguin.serpentine.screens.widgets.ConfigListWidget;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigScreen extends Screen {

    private final Screen parent;
    public final Config config;

    public final Map<String, Object> pendingEdits = new LinkedHashMap<>();

    private ConfigListWidget list;

    public ConfigScreen(Screen parent, Config config) {
        super(Text.literal("Config: " + config.getModId()));
        this.parent = parent;
        this.config = config;
    }

    private Object parseValueFromString(String str, Object defaultVal) {
        if (defaultVal instanceof Boolean) return Boolean.parseBoolean(str);
        if (defaultVal instanceof Integer) return Integer.parseInt(str);
        if (defaultVal instanceof Float) return Float.parseFloat(str);
        if (defaultVal instanceof Double) return Double.parseDouble(str);
        return str; // fallback to string
    }

    public Map<String, Object> getDisplayValues() {
        Map<String, Object> display = new LinkedHashMap<>();
        Map<String, String> serverVals = ConfigNetworkingClient.serverConfigCache
            .getOrDefault(config.getModId(), Map.of());


        for (var entry : config.getValues().entrySet()) {
            String key = entry.getKey();
            SyncSide side = config.getSyncSide(key);

            if (side == Config.SyncSide.SERVER_ONLY || side == SyncSide.SYNCED) {
                if (serverVals.containsKey(key)) {
                    Object val = parseValueFromString(serverVals.get(key), entry.getValue());
                    display.put(key, val);
                } else {
                    display.put(key, entry.getValue());
                }
            } else { // CLIENT_ONLY
                display.put(key, entry.getValue());
            }
        }

        return display;
    }

    @Override
    protected void init() {
        
        int rowHeight = 24;
        int listTop = 40;   // Y coordinate from the top of the screen where the list starts
        int bottomPadding = 40; // The padding you want from the bottom of the screen
        
        // Calculate the Y coordinate where the list should end
        int listBottomY = height - bottomPadding; 
        
        // Calculate the *actual available height* for the list container
        int listAvailableHeight = listBottomY - listTop;

        list = new ConfigListWidget(
            client,
            width,               // Widget width
            listAvailableHeight, // The *available height* for the widget to render within
            listTop,             // Top Y coordinate
            listBottomY,         // Bottom Y coordinate
            rowHeight            // Item height
        );
        // Build entries (exact behavior, just inside rows)
        Map<String, Object> displayValues = getDisplayValues();

        for (Map.Entry<String, Object> entry : displayValues.entrySet()) {
            list.addConfigEntry(
                new ConfigListWidget.ConfigEntry(
                    this,
                    entry.getKey(),
                    entry.getValue(),
                    config,
                    textRenderer
                )
            );
        }

        addSelectableChild(list);

        // Apply button (does not close screen)
        addDrawableChild(
            ButtonWidget.builder(Text.literal("Apply"), btn -> {
                for (var entry : pendingEdits.entrySet()) {
                    String key = entry.getKey();
                    Object newValue = entry.getValue();

                    switch (config.getSyncSide(key)) {
                        case SyncSide.SERVER_ONLY -> {
                            // Send edit to server
                            penguin.serpentine.network.ConfigNetworkingClient.sendC2SRequest(
                                    config.getModId(),
                                    key,
                                    newValue.toString()
                            );
                        }
                        case SyncSide.CLIENT_ONLY -> {
                            // Apply locally
                            config.updateValues(Map.of(key, newValue));
                        }
                        case SyncSide.SYNCED -> {
                            // Do nothing, read-only
                        }
                    }
                }

                // Refresh boolean highlights in the list
                for (var e : list.children()) {
                    if (e instanceof ConfigListWidget.ConfigEntry ce
                            && config.getValues().get(ce.key) instanceof Boolean b) {
                        ce.updateBooleanHighlight(b);
                    }
                }

                pendingEdits.clear();
                MinecraftClient.getInstance().setScreen(parent);
            }).dimensions(10, height - 30, 60, 20).build()
        );


        // Cancel button (closes)
        addDrawableChild(
            ButtonWidget.builder(Text.literal("Cancel"), btn -> {
                MinecraftClient.getInstance().setScreen(parent);
            }).dimensions(80, height - 30, 60, 20).build()
        );
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);

        super.render(ctx, mouseX, mouseY, delta);

        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFFFF);

        list.render(ctx, mouseX, mouseY, delta);

    }
}