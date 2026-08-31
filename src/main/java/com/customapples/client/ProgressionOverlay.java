package com.customapples.client;

import com.customapples.progression.ProgressionManager;
import com.customapples.progression.ProgressionTier;
import com.customapples.progression.UnlockStep;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;

public class ProgressionOverlay {
    public static final IGuiOverlay PROGRESSION_BAR = (gui, graphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ProgressionTier tier = ProgressionManager.getTier(mc.player);
        UnlockStep next = ProgressionManager.getNextCraftStep(mc.player);
        boolean goldenRevealed = ProgressionManager.isGoldenRevealed(mc.player);

        int barWidth = 200;
        int x = (screenWidth - barWidth) / 2;
        int y = 4;
        int segmentWidth = barWidth / ProgressionTier.values().length;

        graphics.fill(x - 2, y - 2, x + barWidth + 2, y + 14, 0x88000000);
        for (ProgressionTier t : ProgressionTier.values()) {
            int sx = x + t.getIndex() * segmentWidth;
            int color = t.getIndex() <= tier.getIndex() ? 0xFFCC3333 : 0xFF444444;
            graphics.fill(sx, y, sx + segmentWidth - 1, y + 10, color);
        }
        graphics.drawCenteredString(mc.font, tier.getDisplayName(), screenWidth / 2, y + 12, 0xFFFFFF);

        int docX = screenWidth - 150;
        int docY = 8;
        int docW = 140;
        int docH = 56;
        graphics.fill(docX, docY, docX + docW, docY + docH, 0xC0202020);
        graphics.drawString(mc.font, "Next Craft", docX + 6, docY + 4, 0xFFCC3333, false);

        if (next.isGoldenTier() && !goldenRevealed) {
            graphics.drawString(mc.font, "[ CENSORED ]", docX + 6, docY + 20, 0xFF888888, false);
            graphics.drawString(mc.font, "Golden tier soon...", docX + 6, docY + 34, 0xFF666666, false);
        } else {
            graphics.drawString(mc.font, next.displayName().getString(), docX + 6, docY + 20, 0xFFFFFF, false);
            String hint = UnlockStep.recipeHint(next);
            if (hint.length() > 22) {
                graphics.drawString(mc.font, hint.substring(0, 22) + "...", docX + 6, docY + 34, 0xFFCCCCCC, false);
            } else {
                graphics.drawString(mc.font, hint, docX + 6, docY + 34, 0xFFCCCCCC, false);
            }
        }
    };

    public static void register(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.PLAYER_LIST.id(), "customapples_progression",
                PROGRESSION_BAR);
    }
}
