package com.customapples.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffers;

public final class AppleVillagerHelper {
    public static final String APPLE_VILLAGER_TAG = "CustomApplesAppleVillager";

    private AppleVillagerHelper() {}

    /** Bell conversion: apple texture + original trades with emeralds swapped to apples. */
    public static void convert(Villager villager) {
        villager.addTag(APPLE_VILLAGER_TAG);
        villager.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.APPLE));
        villager.setDropChance(EquipmentSlot.HEAD, 0.0f);
        villager.setOffers(convertEmeraldsToApples(villager.getOffers()));
    }

    public static MerchantOffers convertEmeraldsToApples(MerchantOffers source) {
        MerchantOffers converted = new MerchantOffers();
        if (source == null) {
            return converted;
        }
        for (net.minecraft.world.item.trading.MerchantOffer offer : source) {
            ItemStack costA = swapEmeraldForApple(offer.getCostA());
            ItemStack costB = swapEmeraldForApple(offer.getCostB());
            converted.add(new net.minecraft.world.item.trading.MerchantOffer(
                    costA,
                    costB,
                    offer.getResult(),
                    offer.getUses(),
                    offer.getMaxUses(),
                    offer.getXp(),
                    offer.getPriceMultiplier()));
        }
        return converted;
    }

    private static ItemStack swapEmeraldForApple(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (stack.is(Items.EMERALD)) {
            return new ItemStack(Items.APPLE, stack.getCount());
        }
        return stack.copy();
    }
}
