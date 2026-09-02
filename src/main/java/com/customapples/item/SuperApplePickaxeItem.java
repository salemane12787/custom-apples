package com.customapples.item;

import com.customapples.util.BlockBreakGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SuperApplePickaxeItem extends PickaxeItem {
    private static final String BATCH_BREAK_TAG = "customapples:super_pick_batch";

    public SuperApplePickaxeItem(Properties properties) {
        super(AppleToolTiers.SUPER, 5, -2.8f, properties);
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
            break3x3x3(level, pos, player, tool);
        } finally {
            player.getPersistentData().remove(BATCH_BREAK_TAG);
        }
    }

    private static void break3x3x3(ServerLevel level, BlockPos center, Player player, ItemStack tool) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    BlockPos target = center.offset(dx, dy, dz);
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
    }
}
