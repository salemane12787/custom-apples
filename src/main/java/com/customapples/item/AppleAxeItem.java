package com.customapples.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.state.BlockState;

public class AppleAxeItem extends AxeItem {
    public AppleAxeItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.is(BlockTags.LEAVES)) {
            return 1.0f;
        }
        if (state.is(BlockTags.LOGS)) {
            return super.getDestroySpeed(stack, state) * 1.5f;
        }
        return super.getDestroySpeed(stack, state);
    }
}
