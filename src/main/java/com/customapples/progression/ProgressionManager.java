package com.customapples.progression;

import com.customapples.util.RecipeUnlocker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ProgressionManager {
    private static final String UNLOCK_INDEX = "CustomApplesUnlockIndex";
    private static final String GOLDEN_REVEALED = "CustomApplesGoldenRevealed";

    private ProgressionManager() {}

    public static int getUnlockIndex(Player player) {
        return player.getPersistentData().getInt(UNLOCK_INDEX);
    }

    public static boolean isGoldenRevealed(Player player) {
        return player.getPersistentData().getBoolean(GOLDEN_REVEALED);
    }

    public static void revealGoldenTier(Player player) {
        player.getPersistentData().putBoolean(GOLDEN_REVEALED, true);
        int goldenStart = UnlockStep.GOLDEN_APPLE_SWORD.ordinal();
        if (getUnlockIndex(player) < goldenStart) {
            player.getPersistentData().putInt(UNLOCK_INDEX, goldenStart);
            RecipeUnlocker.sync(player);
        }
    }

    public static boolean canCraft(Player player, UnlockStep step) {
        if (step.isGoldenTier() && !isGoldenRevealed(player)) {
            return false;
        }
        return step.ordinal() <= getUnlockIndex(player);
    }

    public static UnlockStep getCurrentUnlockStep(Player player) {
        int idx = getUnlockIndex(player);
        UnlockStep[] steps = UnlockStep.values();
        if (idx >= steps.length) {
            return steps[steps.length - 1];
        }
        return steps[idx];
    }

    public static UnlockStep getNextCraftStep(Player player) {
        int next = getUnlockIndex(player) + 1;
        UnlockStep[] steps = UnlockStep.values();
        if (next >= steps.length) {
            return steps[steps.length - 1];
        }
        return steps[next];
    }

    public static void unlockStep(Player player, UnlockStep step) {
        int current = getUnlockIndex(player);
        if (step.ordinal() > current) {
            player.getPersistentData().putInt(UNLOCK_INDEX, step.ordinal());
            RecipeUnlocker.unlockStep(player, step);
        }
    }

    public static void onCraft(Player player, ItemStack result) {
        if (result.isEmpty()) return;
        UnlockStep.forItem(result.getItem()).ifPresent(crafted -> {
            if (crafted == UnlockStep.APPLETIZER) {
                revealGoldenTier(player);
            }
            crafted.next().ifPresent(next -> unlockStep(player, next));
        });
    }

    public static void onLetterAUsed(Player player) {
        unlockStep(player, UnlockStep.APPLE_AXE);
    }

    // Legacy tier API for overlay segments
    public static ProgressionTier getTier(Player player) {
        int idx = getUnlockIndex(player);
        if (idx >= UnlockStep.DRAGON_APPLE.ordinal()) return ProgressionTier.DRAGON_APPLE;
        if (idx >= UnlockStep.END_APPLE.ordinal()) return ProgressionTier.END_TIER;
        if (idx >= UnlockStep.GOLDEN_APPLE_SWORD.ordinal()) return ProgressionTier.GOLDEN_APPLE;
        if (idx >= UnlockStep.LAPIS_APPLE.ordinal()) return ProgressionTier.LAPIS_APPLE;
        if (idx >= UnlockStep.APPLE_BOOTS.ordinal()) return ProgressionTier.APPLE_BOOTS;
        if (idx >= UnlockStep.APPLE_AXE.ordinal()) return ProgressionTier.APPLE_AXE;
        return ProgressionTier.APPLE;
    }

    public static void unlockForItem(Player player, Item item) {
        UnlockStep.forItem(item).ifPresent(step -> unlockStep(player, step));
    }
}
