package penguin.serpentine;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import penguin.serpentine.core.Config;
import penguin.serpentine.core.Serpentine;
import penguin.serpentine.screens.ConfigScreen;

import java.util.List;
import java.util.Optional;

public class SerpentineClient implements ClientModInitializer {

    private static final Text CUSTOM_BUTTON_TEXT = Text.literal("Serpentine");

    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof OptionsScreen) {
                List<ClickableWidget> buttons = Screens.getButtons(screen);

                int bwidth = 200;
                int bheight = 20;
                // Calculate the dynamic position based on the current 'width' and 'height'
                int newX = (width / 2) - (bwidth / 2);
                int newY = height / 6 + 16;
                
                // Find the existing button
                Optional<ClickableWidget> foundButton = buttons.stream()
                    .filter(widget -> widget instanceof ButtonWidget && ((ButtonWidget) widget).getMessage().equals(CUSTOM_BUTTON_TEXT))
                    .findFirst();
                
                if (foundButton.isPresent()) {
                    // If the button exists, update its coordinates to center it based on new screen size
                    foundButton.get().setX(newX);
                    foundButton.get().setY(newY);
                } else {
                    // If the button doesn't exist (first time opening the screen), create it
                    ButtonWidget customButton = ButtonWidget.builder(
                        CUSTOM_BUTTON_TEXT,
                        button -> {
                            openConfigSelectionScreen(screen);
                        }
                    )
                    .position(newX, newY) // Use the dynamic position calculated above
                    .size(bwidth, bheight)
                    .build();

                    buttons.add(customButton);
                }
            }
        });
    }
    
    /** Open a selection screen for all registered configs */
    private void openConfigSelectionScreen(Screen parent) {
        for (Config config : Serpentine.getAllConfigs()) {
            MinecraftClient.getInstance().setScreen(new ConfigScreen(parent, config));
            break;
        }
    }
}