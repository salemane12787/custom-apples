package com.customapples.item;

import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AppleBowItem extends BowItem {
    public AppleBowItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        return super.getDefaultInstance();
    }

    public static boolean isAppleArrow(ItemStack stack) {
        return stack.is(Items.ARROW) || stack.is(Items.APPLE);
    }
}
