package com.customapples.item;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public class GoldenAppleSwordItem extends SwordItem {
    public GoldenAppleSwordItem(Properties properties) {
        super(Tiers.IRON, 1, -2.4f, properties);
    }
}
