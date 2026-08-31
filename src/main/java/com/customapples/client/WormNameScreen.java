package com.customapples.client;

import com.customapples.entity.WormEntity;
import com.customapples.item.ModItems;
import com.customapples.network.ModNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class WormNameScreen extends Screen {
    private static final int GUI_WIDTH = 220;
    private static final int GUI_HEIGHT = 110;

    private final WormEntity worm;
    private final ItemStack wormStack = new ItemStack(ModItems.WORM.get());
    private EditBox nameField;
    private int leftPos;
    private int topPos;

    public WormNameScreen(WormEntity worm) {
        super(Component.translatable("screen.customapples.worm_name"));
        this.worm = worm;
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - GUI_HEIGHT) / 2;

        this.nameField = new EditBox(this.font, this.leftPos + 44, this.topPos + 40, 160, 18,
                Component.translatable("screen.customapples.worm_name_input"));
        this.nameField.setMaxLength(32);
        this.nameField.setFocused(true);
        this.setInitialFocus(this.nameField);
        this.addRenderableWidget(this.nameField);

        int buttonY = this.topPos + 78;
        int buttonWidth = 90;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> {
            String name = this.nameField.getValue().trim();
            if (!name.isEmpty()) {
                ModNetworking.sendWormName(this.worm.getId(), name);
            }
            this.onClose();
        }).bounds(this.leftPos + 16, buttonY, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> this.onClose())
                .bounds(this.leftPos + 114, buttonY, buttonWidth, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        this.renderPanel(graphics);

        Component hint = Component.translatable("screen.customapples.worm_name_hint");
        int hintWidth = this.font.width(hint);
        graphics.drawString(this.font, hint, this.leftPos + (GUI_WIDTH - hintWidth) / 2, this.topPos + 22, 0x606060,
                false);

        int slotX = this.leftPos + 16;
        int slotY = this.topPos + 40;
        this.renderSlot(graphics, slotX, slotY);
        graphics.renderItem(this.wormStack, slotX + 1, slotY + 1);
        graphics.renderItemDecorations(this.font, this.wormStack, slotX + 1, slotY + 1);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderPanel(GuiGraphics graphics) {
        graphics.fill(this.leftPos, this.topPos, this.leftPos + GUI_WIDTH, this.topPos + GUI_HEIGHT, 0xFFC6C6C6);
        graphics.fill(this.leftPos + 1, this.topPos + 1, this.leftPos + GUI_WIDTH - 1, this.topPos + GUI_HEIGHT - 1,
                0xFF000000);
        graphics.fill(this.leftPos + 2, this.topPos + 2, this.leftPos + GUI_WIDTH - 2, this.topPos + GUI_HEIGHT - 2,
                0xFF8B8B8B);

        int titleWidth = this.font.width(this.title);
        graphics.drawString(this.font, this.title, this.leftPos + (GUI_WIDTH - titleWidth) / 2, this.topPos + 8,
                0x404040, false);
    }

    private void renderSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF8B8B8B);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF373737);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (this.nameField.keyPressed(key, scanCode, modifiers) || this.nameField.canConsumeInput()) {
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return this.nameField.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
