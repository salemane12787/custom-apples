package com.customapples.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class GoldenAppleBlock extends Block {
    public GoldenAppleBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static ItemStack transmuteStack(ItemStack input) {
        if (input.isEmpty() || input.is(Items.GOLDEN_APPLE)) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(Items.GOLDEN_APPLE, input.getCount());
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

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1, false, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, false, true, true));
        }
        super.stepOn(level, pos, state, entity);
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
            int count = 8 + level.random.nextInt(57);
            Block.popResource(level, pos, new ItemStack(Items.GOLDEN_APPLE, count));
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }
}
