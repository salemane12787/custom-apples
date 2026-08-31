package com.customapples.item;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class AppleSwordItem extends SwordItem {
    public AppleSwordItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, -1, attackSpeed, properties);
    }
}
