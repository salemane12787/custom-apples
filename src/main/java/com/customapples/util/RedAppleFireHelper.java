package com.customapples.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class RedAppleFireHelper {
    private RedAppleFireHelper() {}

    public static void placeRedAppleFire(Level level, BlockPos pos) {
        if (!level.getBlockState(pos).isAir()) {
            return;
        }
        level.setBlock(pos, com.customapples.item.ModBlocks.RED_APPLE_FIRE.get().defaultBlockState(), 3);
    }

    public static void onMobInFire(net.minecraft.world.entity.LivingEntity entity) {
        if (!entity.getPersistentData().getBoolean("CustomApplesRedFire")) {
            entity.getPersistentData().putBoolean("CustomApplesRedFire", true);
        }
        if (entity.level().isClientSide) {
            return;
        }
        entity.hurt(entity.damageSources().inFire(), 2.5f);
        entity.spawnAtLocation(new ItemStack(Items.APPLE, 1 + entity.level().random.nextInt(2)));
    }

    public static void onMobDeath(Level level, net.minecraft.world.entity.LivingEntity entity) {
        if (entity.getPersistentData().getBoolean("CustomApplesRedFire")) {
            for (int i = 0; i < 6; i++) {
                entity.spawnAtLocation(new ItemStack(Items.APPLE, 2));
            }
        }
    }

    public static void igniteMob(net.minecraft.world.entity.LivingEntity entity) {
        entity.setSecondsOnFire(6);
        entity.getPersistentData().putBoolean("CustomApplesRedFire", true);
    }
}
