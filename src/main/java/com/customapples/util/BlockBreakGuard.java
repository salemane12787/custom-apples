package com.customapples.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.BlockTags;

public final class BlockBreakGuard {
    private BlockBreakGuard() {
    }

    public static boolean isProtected(BlockState state) {
        if (state.isAir()) {
            return true;
        }
        return state.is(Blocks.BEDROCK)
                || state.is(Blocks.END_PORTAL)
                || state.is(Blocks.END_PORTAL_FRAME)
                || state.is(Blocks.NETHER_PORTAL)
                || state.is(Blocks.RESPAWN_ANCHOR)
                || state.is(BlockTags.PORTALS);
    }

    public static boolean canBreak(Level level, BlockPos pos) {
        return !isProtected(level.getBlockState(pos));
    }
}
