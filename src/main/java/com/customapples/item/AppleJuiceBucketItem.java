package com.customapples.item;

import com.customapples.fluid.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Apple bucket filled with apple juice — places juice like water. */
public class AppleJuiceBucketItem extends Item {
    public AppleJuiceBucketItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        BlockHitResult hit = getPlayerPOVHitResult(level, player, net.minecraft.world.level.ClipContext.Fluid.NONE);
        BlockPos pos = hit.getBlockPos();
        BlockPos placePos = pos.relative(hit.getDirection());

        if (!level.mayInteract(player, placePos)) {
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }

        if (!level.isClientSide) {
            BlockState existing = level.getBlockState(placePos);
            if (existing.canBeReplaced() || existing.is(Blocks.WATER)) {
                level.setBlock(placePos, ModFluids.APPLE_JUICE.get().defaultFluidState().createLegacyBlock(), 3);
                level.scheduleTick(placePos, ModFluids.APPLE_JUICE.get(), ModFluids.APPLE_JUICE.get().getTickDelay(level));
                level.playSound(null, placePos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(ModItems.APPLE_BUCKET.get()));
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public net.minecraft.world.InteractionResult useOn(UseOnContext context) {
        return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult()
                == net.minecraft.world.InteractionResult.SUCCESS
                ? net.minecraft.world.InteractionResult.SUCCESS
                : net.minecraft.world.InteractionResult.PASS;
    }
}
