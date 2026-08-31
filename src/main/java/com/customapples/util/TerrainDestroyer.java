package com.customapples.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class TerrainDestroyer {
    private TerrainDestroyer() {}

    public static void blastCylinderDown(Level level, BlockPos start) {
        int radius = 10;
        int minY = level.getMinBuildHeight();
        int topY = start.getY() + 40;
        level.playSound(null, start, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 2.0f, 0.4f);
        level.playSound(null, start, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.BLOCKS, 1.0f, 0.3f);
        if (level instanceof ServerLevel serverLevel) {
            for (int y = start.getY(); y <= topY; y++) {
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        start.getX() + 0.5, y + 0.5, start.getZ() + 0.5, 3, 0.1, 0.5, 0.1, 0.02);
            }
        }
        for (int y = start.getY(); y >= minY; y--) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz > radius * radius) {
                        continue;
                    }
                    BlockPos pos = new BlockPos(start.getX() + dx, y, start.getZ() + dz);
                    BlockState blockState = level.getBlockState(pos);
                    if (BlockBreakGuard.isProtected(blockState)) {
                        continue;
                    }
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    if (level instanceof ServerLevel serverLevel && (dx * dx + dz * dz) <= 6) {
                        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                2, 0.1, 0.1, 0.1, 0.02);
                    }
                }
            }
        }
    }

    public static void transmuteToDiamonds(Level level, Player player, int radius) {
        BlockPos center = player.blockPosition();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radius * radius) {
                        continue;
                    }
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (BlockBreakGuard.isProtected(state)) {
                        continue;
                    }
                    level.setBlock(pos, Blocks.DIAMOND_BLOCK.defaultBlockState(), 3);
                }
            }
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack slot = player.getInventory().getItem(i);
            if (!slot.isEmpty()) {
                player.getInventory().setItem(i, new ItemStack(Items.DIAMOND, slot.getCount()));
            }
        }
        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 40; i++) {
                ItemEntity diamond = new ItemEntity(level,
                        player.getX() + (level.random.nextDouble() - 0.5) * 6,
                        player.getY() + 8 + level.random.nextDouble() * 4,
                        player.getZ() + (level.random.nextDouble() - 0.5) * 6,
                        new ItemStack(Items.DIAMOND, 1 + level.random.nextInt(3)));
                diamond.setDeltaMovement(0, -0.2, 0);
                level.addFreshEntity(diamond);
            }
            serverLevel.sendParticles(ParticleTypes.FIREWORK,
                    player.getX(), player.getY() + 1, player.getZ(), 50, 2, 2, 2, 0.1);
        }
    }
}
