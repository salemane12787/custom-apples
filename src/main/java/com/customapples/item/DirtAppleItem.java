package com.customapples.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DirtAppleItem extends Item {
    public DirtAppleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 400, 1));
            if (!player.getInventory().add(new ItemStack(ModItems.WORM.get()))) {
                player.drop(new ItemStack(ModItems.WORM.get()), false);
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
