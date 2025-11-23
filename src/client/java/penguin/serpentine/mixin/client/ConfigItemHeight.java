package penguin.serpentine.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.EntryListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import penguin.serpentine.screens.ConfigScreen; // import your ConfigScreen

@Mixin(EntryListWidget.class)
public abstract class ConfigItemHeight {

    @Mutable
    @Shadow
    protected int itemHeight;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        Screen current = MinecraftClient.getInstance().currentScreen;
        if (current instanceof ConfigScreen) { // Only apply if current screen is ConfigScreen
            this.itemHeight = 24; // set your desired row height
        }
    }
}