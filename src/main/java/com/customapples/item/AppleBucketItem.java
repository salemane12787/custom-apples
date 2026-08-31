package com.customapples.item;

import com.customapples.util.AppleTreeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Empty apple bucket — fills with water or apple juice. */
public class AppleBucketItem extends Item {
    public AppleBucketItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();

        if (player == null) {
            return net.minecraft.world.InteractionResult.PASS;
        }

        if (state.is(BlockTags.LEAVES)) {
            if (!level.isClientSide) {
                player.setItemInHand(hand, new ItemStack(ModItems.APPLE_BUCKET_JUICE.get()));
                AppleTreeHelper.collapseLeavesCanopy(level, pos, player);
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (state.is(Blocks.WATER) && state.getFluidState().isSource()) {
            if (!level.isClientSide) {
                player.setItemInHand(hand, new ItemStack(ModItems.APPLE_BUCKET_WATER.get()));
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
        }

        return net.minecraft.world.InteractionResult.PASS;
    }
}
