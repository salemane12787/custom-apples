package com.customapples.util;

import com.customapples.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.TickTask;
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
    private static final int LEAF_MARGIN = 7;

    private TreeCapitatorHelper() {}

    public static boolean isLog(BlockState state) {
        return state.is(BlockTags.LOGS);
    }

    public static boolean isLeaves(BlockState state) {
        return state.is(BlockTags.LEAVES);
    }

    /**
     * Breaks an entire connected tree: logs drop wooden apples (never vanilla logs),
     * then leaves decay quickly with vanilla-style drops.
     */
    public static void breakTree(ServerLevel level, BlockPos start, Player player, ItemStack tool) {
        List<BlockPos> logs = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        collectLogs(level, start, visited, logs, MAX_LOGS);
        if (logs.isEmpty()) {
            return;
        }

        List<BlockPos> leaves = collectLeavesNearLogs(level, logs);

        for (BlockPos pos : logs) {
            BlockState state = level.getBlockState(pos);
            if (!isLog(state)) {
                continue;
            }
            // No log drops — wooden apples only
            level.destroyBlock(pos, false);
            int count = 1 + level.getRandom().nextInt(2);
            Block.popResource(level, pos, new ItemStack(ModItems.WOODEN_APPLE.get(), count));
            if (tool.isDamageableItem()) {
                tool.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
            }
        }

        decayLeavesFast(level, leaves, player, tool);
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

    private static List<BlockPos> collectLeavesNearLogs(Level level, List<BlockPos> logs) {
        if (logs.isEmpty()) {
            return List.of();
        }
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos log : logs) {
            minX = Math.min(minX, log.getX() - LEAF_MARGIN);
            minY = Math.min(minY, log.getY() - LEAF_MARGIN);
            minZ = Math.min(minZ, log.getZ() - LEAF_MARGIN);
            maxX = Math.max(maxX, log.getX() + LEAF_MARGIN);
            maxY = Math.max(maxY, log.getY() + LEAF_MARGIN);
            maxZ = Math.max(maxZ, log.getZ() + LEAF_MARGIN);
        }

        List<BlockPos> leaves = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(p);
                    if (isLeaves(state)) {
                        leaves.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        return leaves;
    }

    /**
     * Removes leaves in quick staggered waves (vanilla drops, faster than natural decay).
     */
    private static void decayLeavesFast(ServerLevel level, List<BlockPos> leaves, Player player, ItemStack tool) {
        if (leaves.isEmpty()) {
            return;
        }
        List<BlockPos> shuffled = new ArrayList<>(leaves);
        for (int i = shuffled.size() - 1; i > 0; i--) {
            int j = level.getRandom().nextInt(i + 1);
            BlockPos tmp = shuffled.get(i);
            shuffled.set(i, shuffled.get(j));
            shuffled.set(j, tmp);
        }
        int baseTick = level.getServer().getTickCount();

        for (int i = 0; i < shuffled.size(); i++) {
            BlockPos pos = shuffled.get(i);
            // Spread over ~10 ticks so leaves vanish quickly but still feel natural
            int runAt = baseTick + 1 + (i % 3);
            level.getServer().tell(new TickTask(runAt, () -> {
                BlockState state = level.getBlockState(pos);
                if (!isLeaves(state)) {
                    return;
                }
                // Vanilla leaf drops (saplings, apples, sticks) then remove block
                Block.dropResources(state, level, pos, null, player, tool);
                level.destroyBlock(pos, false);
                level.levelEvent(2001, pos, Block.getId(state));
            }));
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
