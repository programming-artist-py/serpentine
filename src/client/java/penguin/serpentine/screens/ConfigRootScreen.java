package penguin.serpentine.screens;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import penguin.serpentine.core.Config;
import penguin.serpentine.core.Serpentine;

public class ConfigRootScreen extends Screen {

    private final Screen parent;

    public ConfigRootScreen(Screen parent) {
        super(Text.literal("Serpentine Configs"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        int y = 40;

        for (Config cfg : Serpentine.getAllConfigs()) {

            addDrawableChild(
                ButtonWidget.builder(Text.literal(cfg.getModId()), btn -> {
                    // Open the config editor for THIS file
                    MinecraftClient.getInstance()
                        .setScreen(new ConfigScreen(this, cfg));
                }).dimensions(width / 2 - 100, y, 200, 20).build()
            );

            y += 25;
        }

        // back button
        addDrawableChild(
            ButtonWidget.builder(Text.literal("Back"), btn -> {
                MinecraftClient.getInstance().setScreen(parent);
            }).dimensions(width / 2 - 100, height - 30, 200, 20).build()
        );
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);

        super.render(ctx, mouseX, mouseY, delta);

        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFFFF);

    }
}