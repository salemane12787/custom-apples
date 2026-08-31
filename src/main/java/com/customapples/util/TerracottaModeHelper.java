package com.customapples.util;

import com.customapples.item.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public final class TerracottaModeHelper {
    public static final String TERRACOTTA_TAG = "CustomApplesTerracotta";

    private TerracottaModeHelper() {}

    public static void ringBell(Level level, BlockPos pos, int radius) {
        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 40; i++) {
                serverLevel.sendParticles(
                        ParticleTypes.FALLING_NECTAR,
                        pos.getX() + 0.5,
                        pos.getY() + 1.0,
                        pos.getZ() + 0.5,
                        1,
                        0.4,
                        0.3,
                        0.4,
                        0.02);
            }
        }
        List<LivingEntity> entities =
                level.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(radius));
        for (LivingEntity entity : entities) {
            if (entity instanceof Villager villager && !villager.isBaby()) {
                AppleVillagerHelper.convert(villager);
            } else if (entity instanceof Mob mob && !mob.isBaby()) {
                BlockPos statuePos = mob.blockPosition();
                level.setBlock(statuePos, ModBlocks.TERRACOTTA_STATUE.get().defaultBlockState(), 3);
                mob.discard();
            }
        }
    }

    public static boolean isTerracotta(LivingEntity entity) {
        return entity.getPersistentData().getBoolean(TERRACOTTA_TAG);
    }

    public static boolean isStatueBlock(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() == ModBlocks.TERRACOTTA_STATUE.get();
    }
}
