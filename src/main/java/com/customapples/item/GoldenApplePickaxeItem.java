package com.customapples.item;

import com.customapples.progression.ProgressionManager;
import com.customapples.util.AppleTreeHelper;
import com.customapples.util.BlockBreakGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class GoldenApplePickaxeItem extends PickaxeItem {
    public GoldenApplePickaxeItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, 4, attackSpeed, properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (BlockBreakGuard.isProtected(state)) {
            return 0.0f;
        }
        if (state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN)
                || state.is(Blocks.NETHERITE_BLOCK) || state.is(Blocks.DIAMOND_BLOCK)
                || state.is(Blocks.ANCIENT_DEBRIS)) {
            return 32.0f;
        }
        return super.getDestroySpeed(stack, state);
    }

    public static void onBlockMined(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide) {
            AppleTreeHelper.pickaxeTreeHarvest(level, pos);
            Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 64, false);
            if (player != null) {
                ProgressionManager.unlockForItem(player, ModItems.GOLDEN_APPLE_PICKAXE.get());
            }
        }
    }
}
