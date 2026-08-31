package com.customapples.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;

public final class LootOnHitHelper {
    private LootOnHitHelper() {}

    /** Drops a small slice of the mob's loot table (simulates biting without killing). */
    public static void dropHitLoot(Player player, LivingEntity target, int maxStacks) {
        if (!(target.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        ResourceLocation tableId = target.getLootTable();
        LootTable table = serverLevel.getServer().getLootData().getLootTable(tableId);
        DamageSource source = player.damageSources().playerAttack(player);
        LootParams params = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.THIS_ENTITY, target)
                .withParameter(LootContextParams.ORIGIN, target.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, source)
                .withOptionalParameter(LootContextParams.KILLER_ENTITY, player)
                .create(LootContextParamSets.ENTITY);
        List<ItemStack> loot = table.getRandomItems(params);
        int dropped = 0;
        for (ItemStack stack : loot) {
            if (stack.isEmpty() || dropped >= maxStacks) {
                break;
            }
            ItemStack bite = stack.copy();
            bite.setCount(Math.max(1, Math.min(stack.getCount(), 1 + serverLevel.random.nextInt(2))));
            target.spawnAtLocation(bite);
            dropped++;
        }
    }
}
