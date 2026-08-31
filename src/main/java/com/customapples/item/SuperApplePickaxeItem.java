package com.customapples.item;

import com.customapples.util.AppleTreeHelper;
import com.customapples.util.BlockBreakGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SuperApplePickaxeItem extends PickaxeItem {
    private static final String BATCH_BREAK_TAG = "customapples:super_pick_batch";

    public SuperApplePickaxeItem(Properties properties) {
        super(Tiers.NETHERITE, 5, -2.8f, properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (BlockBreakGuard.isProtected(state)) {
            return 0.0f;
        }
        return super.getDestroySpeed(stack, state);
    }

    public static void onBlockMined(ServerLevel level, BlockPos pos, Player player, ItemStack tool) {
        if (player.getPersistentData().getBoolean(BATCH_BREAK_TAG)) {
            return;
        }
        player.getPersistentData().putBoolean(BATCH_BREAK_TAG, true);
        try {
            break3x3(level, pos, player, tool);
            AppleTreeHelper.sproutMiningTree(level, pos, false);
        } finally {
            player.getPersistentData().remove(BATCH_BREAK_TAG);
        }
    }

    private static void break3x3(ServerLevel level, BlockPos center, Player player, ItemStack tool) {
        Direction.Axis planeAxis = miningPlaneAxis(player);
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                BlockPos target = offsetOnPlane(center, planeAxis, a, b);
                if (target.equals(center)) {
                    continue;
                }
                BlockState state = level.getBlockState(target);
                if (!BlockBreakGuard.canBreak(level, target)) {
                    continue;
                }
                if (!tool.isCorrectToolForDrops(state)) {
                    continue;
                }
                if (state.getDestroySpeed(level, target) < 0.0f) {
                    continue;
                }
                Block.dropResources(state, level, target, null, player, tool);
                level.destroyBlock(target, false);
                level.levelEvent(2001, target, Block.getId(state));
                if (tool.isDamageableItem()) {
                    tool.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
                }
            }
        }
    }

    private static Direction.Axis miningPlaneAxis(Player player) {
        double lookX = player.getLookAngle().x;
        double lookY = player.getLookAngle().y;
        double lookZ = player.getLookAngle().z;
        if (Math.abs(lookY) >= Math.abs(lookX) && Math.abs(lookY) >= Math.abs(lookZ)) {
            return Direction.Axis.Y;
        }
        if (Math.abs(lookX) >= Math.abs(lookZ)) {
            return Direction.Axis.X;
        }
        return Direction.Axis.Z;
    }

    private static BlockPos offsetOnPlane(BlockPos center, Direction.Axis axis, int a, int b) {
        switch (axis) {
            case Y:
                return center.offset(a, 0, b);
            case X:
                return center.offset(0, a, b);
            case Z:
                return center.offset(a, b, 0);
            default:
                return center;
        }
    }
}
