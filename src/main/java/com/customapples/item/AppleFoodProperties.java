package com.customapples.item;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

public final class AppleFoodProperties {
    /** Same eat speed and hunger feel as a vanilla apple. */
    public static FoodProperties standard() {
        return new FoodProperties.Builder().nutrition(4).saturationMod(0.3f).build();
    }

    public static Item.Properties asFood() {
        return new Item.Properties().food(standard());
    }

    private AppleFoodProperties() {
    }
}
