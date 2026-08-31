package com.customapples.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public class SuperAppleSwordItem extends SwordItem {
    public SuperAppleSwordItem(Properties properties) {
        super(Tiers.NETHERITE, 3, -2.4f, properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
