package com.customapples.util;

import com.customapples.CustomApplesMod;
import com.customapples.progression.UnlockStep;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RecipeUnlocker {
    private RecipeUnlocker() {}

    public static void sync(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        int index = com.customapples.progression.ProgressionManager.getUnlockIndex(player);
        List<Recipe<?>> recipes = new ArrayList<>();
        UnlockStep[] steps = UnlockStep.values();
        for (int i = 0; i <= index && i < steps.length; i++) {
            findRecipe(serverPlayer, steps[i]).ifPresent(recipes::add);
        }
        if (!recipes.isEmpty()) {
            serverPlayer.awardRecipes(recipes);
        }
    }

    public static void unlockStep(Player player, UnlockStep step) {
        if (player instanceof ServerPlayer serverPlayer) {
            findRecipe(serverPlayer, step).ifPresent(recipe -> serverPlayer.awardRecipes(List.of(recipe)));
        }
    }

    private static Optional<Recipe<?>> findRecipe(ServerPlayer player, UnlockStep step) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CustomApplesMod.MOD_ID, step.getSerializedName());
        return player.getServer().getRecipeManager().byKey(id).map(recipe -> recipe);
    }
}
