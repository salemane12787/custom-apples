package com.customapples.item;

import com.customapples.effect.ModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BreadAppleItem extends Item {
    /** Omnivore lasts five minutes. */
    public static final int OMNIVORE_DURATION_TICKS = 6000;

    public BreadAppleItem(Properties properties) {
        super(properties);
    }

    public static boolean isOmnivoreActive(Player player) {
        return player.hasEffect(ModMobEffects.OMNIVORE.get());
    }

    public static void grantOmnivore(Player player) {
        player.addEffect(new MobEffectInstance(
                ModMobEffects.OMNIVORE.get(),
                OMNIVORE_DURATION_TICKS,
                0,
                false,
                true,
                true));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            grantOmnivore(player);
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public net.minecraft.world.item.UseAnim getUseAnimation(ItemStack stack) {
        return net.minecraft.world.item.UseAnim.EAT;
    }
}
