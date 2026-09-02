package com.customapples.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

public class SuperAppleSwordItem extends SwordItem {
    public SuperAppleSwordItem(Properties properties) {
        super(AppleToolTiers.SUPER, 3, -2.4f, properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
