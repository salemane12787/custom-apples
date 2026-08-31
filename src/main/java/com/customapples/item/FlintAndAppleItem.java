package com.customapples.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class FlintAndAppleItem extends FlintAndSteelItem {
    public FlintAndAppleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        BlockPos firePos = clicked.relative(context.getClickedFace());
        BlockState fireState = level.getBlockState(firePos);

        if (!fireState.isAir() && !fireState.canBeReplaced()) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            level.setBlock(firePos, ModBlocks.RED_APPLE_FIRE.get().defaultBlockState(), 11);
            level.playSound(null, firePos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            ItemStack stack = context.getItemInHand();
            if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                stack.hurtAndBreak(1, context.getPlayer(), p -> p.broadcastBreakEvent(context.getHand()));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
