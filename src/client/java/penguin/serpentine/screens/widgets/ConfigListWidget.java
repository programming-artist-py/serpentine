package penguin.serpentine.screens.widgets;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import penguin.serpentine.core.Config;
import penguin.serpentine.core.Config.SyncSide;
import penguin.serpentine.network.ConfigNetworkingClient;
import penguin.serpentine.screens.ConfigScreen;

public class ConfigListWidget extends EntryListWidget<ConfigListWidget.ConfigEntry> {

    protected final int top;
    protected final int bottom;
    protected final int width;

    public ConfigListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
        super(client, width, height, top, bottom);
        this.top = top;
        this.bottom = bottom;
        this.width = width;
    }

    public void addConfigEntry(ConfigEntry entry) {
        super.addEntry(entry);  // legally calls protected method
    }

    public static class ConfigEntry extends ElementListWidget.Entry<ConfigEntry> {

        public final String key;
        public Object value;

        private final ConfigScreen parent;

        // widgets (depending on type)
        private ButtonWidget trueBtn;
        private ButtonWidget falseBtn;
        private SliderWidget slider;
        private TextFieldWidget textField;

        private final TextRenderer tr;
        boolean editable;
        private final SyncSide side;

        public ConfigEntry(ConfigScreen parent, String key, Object value, Config config, TextRenderer tr) {
            this.parent = parent;
            this.key = key;
            this.value = value;
            this.tr = tr;
            this.side = config.getSyncSide(key);
        }

        @Override
        public void render(DrawContext ctx, int idx, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float delta) {

            int widgetX = x + 80;
            int labelY = y + (32 - tr.fontHeight) / 2;

            boolean readOnly;
            if (side == SyncSide.CLIENT_ONLY) {
                readOnly = false;
            } else if (side == SyncSide.SERVER_ONLY) {
                readOnly = !MinecraftClient.getInstance().isInSingleplayer() && !ConfigNetworkingClient.isOp; 
            } else { // SYNCED
                readOnly = true;
            }

            // draw label
            ctx.drawText(tr, Text.literal(key), x - 100, labelY, 0xFFFFFF, false);
            if (value instanceof Boolean b) {
                if (trueBtn == null) {
                    trueBtn = ButtonWidget.builder(Text.literal("True"), btn -> {
                        parent.pendingEdits.put(key, true);
                        updateBooleanHighlight(true);
                    }).dimensions(widgetX, y, 50, 20).build();

                    falseBtn = ButtonWidget.builder(Text.literal("False"), btn -> {
                        parent.pendingEdits.put(key, false);
                        updateBooleanHighlight(false);
                    }).dimensions(widgetX + 60, y, 50, 20).build();

                    updateBooleanHighlight(b);
                }

                trueBtn.setY(y + 2);
                falseBtn.setY(y + 2);

                trueBtn.active = !readOnly;
                falseBtn.active = !readOnly;

                trueBtn.render(ctx, mouseX, mouseY, delta);
                falseBtn.render(ctx, mouseX, mouseY, delta);

            } else if (value instanceof Integer i) {

                if (slider == null) {
                    float v = i / 100f;

                    slider = new SliderWidget(widgetX, y + 2, 200, 20,
                            Text.literal(key + ": " + i), v) {
                        @Override
                        protected void updateMessage() {
                            int val = (int) (value * 100);
                            setMessage(Text.literal(key + ": " + val));
                        }

                        @Override
                        protected void applyValue() {
                            int val = (int) (value * 100);
                            parent.pendingEdits.put(key, val);
                        }
                    };
                }

                slider.setY(y + 2);

                slider.active = !readOnly;
                slider.render(ctx, mouseX, mouseY, delta);
            } else if (value instanceof Float) {

                if (textField == null) {
                    textField = new TextFieldWidget(
                            tr, widgetX, y + 2, 200, 20, Text.literal("")
                    );
                    textField.setText(value.toString());
                    textField.setChangedListener(v -> parent.pendingEdits.put(key, v));
                }

                textField.setY(y + 2);
                textField.active = !readOnly;

                textField.render(ctx, mouseX, mouseY, delta);
                if (textField.active) {
                    ctx.drawCenteredTextWithShadow(tr, Text.literal("F"), textField.getRight() - 10, labelY - 3, 0xFFFFFF);
                } else {
                    ctx.drawCenteredTextWithShadow(tr, Text.literal("F"), textField.getRight() - 10, labelY - 3, 0xA1A1A1);
                }

            } else {

                if (textField == null) {
                    textField = new TextFieldWidget(
                            tr, widgetX, y + 2, 200, 20, Text.literal("")
                    );
                    textField.setText(value.toString());
                    textField.setChangedListener(v -> parent.pendingEdits.put(key, v));
                }

                textField.setY(y + 2);
                textField.active = !readOnly;
                textField.render(ctx, mouseX, mouseY, delta);
            }
        }

        public void updateBooleanHighlight(boolean value) {
            if (trueBtn == null || falseBtn == null) return;

            if (value) {
                trueBtn.setMessage(Text.literal("✔ True"));
                falseBtn.setMessage(Text.literal("False"));
            } else {
                trueBtn.setMessage(Text.literal("True"));
                falseBtn.setMessage(Text.literal("✔ False"));
            }
        }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Element child : this.children()) {
            if (child.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(child);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Element child : this.children()) {
            if (child.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (Element child : this.children()) {
            if (child.charTyped(chr, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Element child : this.children()) {
            if (child.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        for (Element child : this.children()) {
            if (child.mouseDragged(mouseX, mouseY, button, dx, dy)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public List<? extends Element> children() {
        List<Element> list = new ArrayList<>();
        if (trueBtn != null) list.add(trueBtn);
        if (falseBtn != null) list.add(falseBtn);
        if (slider != null) list.add(slider);
        if (textField != null) list.add(textField);
        return list;
    }


    @Override
    public List<? extends Selectable> selectableChildren() {
        List<Selectable> list = new ArrayList<>();
        if (trueBtn != null) list.add(trueBtn);
        if (falseBtn != null) list.add(falseBtn);
        if (slider != null) list.add(slider);
        if (textField != null) list.add(textField);
        return list;
    }

}


    @Override
    public int getRowWidth() {
        return 400; // width the entries need
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        // dunno what to put here
    }

    @Override
    protected void renderList(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int left = this.width / 2 - this.getRowWidth() / 2 + 100;
        int rowWidth = this.getRowWidth();

        for (int i = 0; i < this.getEntryCount(); i++) {
            ConfigEntry entry = this.getEntry(i);
            int y = this.getRowTop(i);

            // Don't draw selection background at all
            entry.render(ctx, i, y, left, rowWidth, this.itemHeight, mouseX, mouseY, false, delta);
        }
    }

}