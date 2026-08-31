package com.customapples.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class AppleTreeHelper {
    private AppleTreeHelper() {}

    public static void collapseLeavesCanopy(Level level, BlockPos start, Player player) {
        for (int dx = -6; dx <= 6; dx++) {
            for (int dy = -4; dy <= 8; dy++) {
                for (int dz = -6; dz <= 6; dz++) {
                    BlockPos p = start.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(p);
                    if (state.getBlock() instanceof LeavesBlock) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                        if (player != null) {
                            player.spawnAtLocation(new ItemStack(Items.APPLE, 2), 0);
                        }
                    }
                }
            }
        }
    }

    public static void spawnGiantAppleTrees(Level level, BlockPos center, int count) {
        for (int i = 0; i < count; i++) {
            BlockPos base = center.offset(level.random.nextInt(12) - 6, 0, level.random.nextInt(12) - 6);
            growAppleTree(level, base);
        }
    }

    /** Grows a pickaxe-shaped burst: breaks blocks in pattern, drops wood/apples/saplings. */
    public static void pickaxeTreeHarvest(Level level, BlockPos mined) {
        if (level.isClientSide) {
            return;
        }
        for (BlockPos p : pickaxePattern(mined)) {
            BlockState existing = level.getBlockState(p);
            if (BlockBreakGuard.isProtected(existing)) {
                continue;
            }
            if (!existing.isAir()) {
                Block.dropResources(existing, level, p, null, null, ItemStack.EMPTY);
            }
            level.setBlock(p, Blocks.OAK_LOG.defaultBlockState(), 3);
            Block.dropResources(Blocks.OAK_LOG.defaultBlockState(), level, p, null, null, ItemStack.EMPTY);
            level.setBlock(p, Blocks.OAK_LEAVES.defaultBlockState(), 3);
            Block.dropResources(Blocks.OAK_LEAVES.defaultBlockState(), level, p, null, null, ItemStack.EMPTY);
            level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private static java.util.List<BlockPos> pickaxePattern(BlockPos center) {
        java.util.List<BlockPos> positions = new java.util.ArrayList<>();
        for (int dx = -3; dx <= 4; dx++) {
            positions.add(center.offset(dx, 0, 0));
        }
        for (int dy = 1; dy <= 8; dy++) {
            positions.add(center.offset(0, dy, 0));
        }
        return positions;
    }

    /** Grows a small apple tree when mining. Optional tiny break — no chunk-sized clears. */
    public static void sproutMiningTree(Level level, BlockPos pos, boolean breakNearby) {
        growAppleTree(level, pos);
        if (!breakNearby) {
            return;
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (level.random.nextFloat() < 0.35f) {
                        BlockPos p = pos.offset(dx, dy, dz);
                        if (BlockBreakGuard.canBreak(level, p)) {
                            level.destroyBlock(p, true);
                        }
                    }
                }
            }
        }
    }

    public static void spawnWorldAppleTree(Level level, BlockPos center) {
        BlockPos base = center;
        for (int y = 0; y < 40; y++) {
            level.setBlock(base.above(y), Blocks.OAK_LOG.defaultBlockState(), 3);
        }
        for (int dx = -8; dx <= 8; dx++) {
            for (int dy = 30; dy <= 45; dy++) {
                for (int dz = -8; dz <= 8; dz++) {
                    if (dx * dx + dz * dz + (dy - 35) * (dy - 35) < 64) {
                        BlockPos leaf = base.offset(dx, dy, dz);
                        level.setBlock(leaf, Blocks.OAK_LEAVES.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    public static void growAppleTreePublic(Level level, BlockPos base) {
        growAppleTree(level, base);
    }

    private static void growAppleTree(Level level, BlockPos base) {
        BlockPos ground = base;
        while (level.getBlockState(ground).isAir() && ground.getY() > level.getMinBuildHeight()) {
            ground = ground.below();
        }
        ground = ground.above();
        int height = 4 + level.random.nextInt(3);
        for (int y = 0; y < height; y++) {
            level.setBlock(ground.above(y), Blocks.OAK_LOG.defaultBlockState(), 3);
        }
        BlockPos top = ground.above(height);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) + Math.abs(dy) < 4) {
                        level.setBlock(top.offset(dx, dy, dz), Blocks.OAK_LEAVES.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
}
