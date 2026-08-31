package com.customapples.util;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

public final class VillagerSplitHelper {
    private VillagerSplitHelper() {}

    public static void splitVillager(Level level, Villager original) {
        BlockPos pos = original.blockPosition();
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0f, 1.2f);
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                2.0f, false, Level.ExplosionInteraction.NONE);

        MerchantOffers converted = AppleVillagerHelper.convertEmeraldsToApples(original.getOffers());

        for (int i = 0; i < 4; i++) {
            Villager mini = net.minecraft.world.entity.EntityType.VILLAGER.create(level);
            if (mini == null) {
                continue;
            }
            mini.moveTo(pos.getX() + 0.5 + (i % 2) * 0.5, pos.getY(), pos.getZ() + 0.5 + (i / 2) * 0.5,
                    original.getYRot(), 0);
            mini.setVillagerData(original.getVillagerData().setLevel(5));
            mini.setBaby(true);
            mini.setAge(-24000);
            mini.setOffers(copyOffers(converted));
            mini.addTag(AppleVillagerHelper.APPLE_VILLAGER_TAG);
            mini.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,
                    new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.APPLE));
            mini.setDropChance(net.minecraft.world.entity.EquipmentSlot.HEAD, 0.0f);
            level.addFreshEntity(mini);
        }
        original.discard();
    }

    private static MerchantOffers copyOffers(MerchantOffers source) {
        MerchantOffers copy = new MerchantOffers();
        for (MerchantOffer offer : source) {
            copy.add(new MerchantOffer(
                    offer.getCostA().copy(),
                    offer.getCostB().copy(),
                    offer.getResult().copy(),
                    offer.getUses(),
                    offer.getMaxUses(),
                    offer.getXp(),
                    offer.getPriceMultiplier()));
        }
        return copy;
    }
}
