package com.customapples.util;

import com.customapples.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TreeCapitatorHelper {
    private static final int MAX_LOGS = 512;

    private TreeCapitatorHelper() {}

    public static boolean isLog(BlockState state) {
        return state.is(BlockTags.LOGS);
    }

    public static boolean isLeaves(BlockState state) {
        return state.is(BlockTags.LEAVES);
    }

    /**
     * Breaks connected logs only — wooden apples drop, leaves decay naturally.
     */
    public static void breakTree(ServerLevel level, BlockPos start, Player player, ItemStack tool) {
        List<BlockPos> logs = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        collectLogs(level, start, visited, logs, MAX_LOGS);
        if (logs.isEmpty()) {
            return;
        }

        for (BlockPos pos : logs) {
            BlockState state = level.getBlockState(pos);
            if (!isLog(state)) {
                continue;
            }
            level.destroyBlock(pos, false);
            int count = 1 + level.getRandom().nextInt(2);
            Block.popResource(level, pos, new ItemStack(ModItems.WOODEN_APPLE.get(), count));
            if (tool.isDamageableItem()) {
                tool.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
            }
            nudgeNearbyLeaves(level, pos);
        }
    }

    private static void nudgeNearbyLeaves(ServerLevel level, BlockPos logPos) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos leafPos = logPos.offset(dx, dy, dz);
                    nudgeLeafDecay(level, leafPos);
                }
            }
        }
    }

    private static void collectLogs(Level level, BlockPos pos, Set<BlockPos> visited, List<BlockPos> logs, int max) {
        if (logs.size() >= max) {
            return;
        }
        if (!isLog(level.getBlockState(pos))) {
            return;
        }
        BlockPos immutable = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
        if (!visited.add(immutable)) {
            return;
        }
        logs.add(immutable);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    collectLogs(level, immutable.offset(dx, dy, dz), visited, logs, max);
                }
            }
        }
    }

    /** Accelerate natural leaf decay when a log neighbor was removed. */
    public static void nudgeLeafDecay(ServerLevel level, BlockPos leafPos) {
        BlockState state = level.getBlockState(leafPos);
        if (state.getBlock() instanceof LeavesBlock leaves) {
            leaves.tick(state, level, leafPos, level.getRandom());
            level.scheduleTick(leafPos, state.getBlock(), 2 + level.getRandom().nextInt(4));
        }
    }
}
