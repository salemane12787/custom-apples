package com.customapples.item;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class SplinterItem extends SwordItem {
    public SplinterItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, 7, attackSpeed, properties);
    }
}
