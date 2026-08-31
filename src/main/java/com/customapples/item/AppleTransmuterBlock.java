package com.customapples.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class AppleTransmuterBlock extends Block {
    public AppleTransmuterBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static void transmuteItem(Level level, BlockPos pos, ItemEntity itemEntity) {
        if (level.isClientSide) {
            return;
        }
        ItemStack input = itemEntity.getItem();
        ItemStack output = transmuteStack(input);
        if (output.isEmpty()) {
            return;
        }
        itemEntity.discard();
        ItemEntity result = new ItemEntity(
                level,
                pos.getX() + 0.5,
                pos.getY() + 1.0,
                pos.getZ() + 0.5,
                output);
        result.setDefaultPickUpDelay();
        level.addFreshEntity(result);
    }

    public static ItemStack transmuteStack(ItemStack input) {
        if (input.isEmpty() || input.is(Items.APPLE)) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(Items.APPLE, input.getCount());
    }

    @Override
    public void playerDestroy(
            Level level,
            Player player,
            BlockPos pos,
            BlockState state,
            net.minecraft.world.level.block.entity.BlockEntity blockEntity,
            ItemStack tool) {
        if (!level.isClientSide) {
            int count = 16 + level.random.nextInt(49);
            Block.popResource(level, pos, new ItemStack(Items.APPLE, count));
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }
}
