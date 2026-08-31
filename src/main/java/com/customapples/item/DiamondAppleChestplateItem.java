package com.customapples.item;

import com.customapples.effect.ModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class DiamondAppleChestplateItem extends Item {
    private static final UUID HEALTH_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    /** Shows as infinite in the effect HUD. */
    private static final int INFINITE_TICKS = 1999980;

    public DiamondAppleChestplateItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE, INFINITE_TICKS, 2, false, true, true));
            player.addEffect(new MobEffectInstance(
                    ModMobEffects.WIDE.get(), INFINITE_TICKS, 0, false, false, true));
            AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
            if (health != null) {
                health.removeModifier(HEALTH_UUID);
                health.addTransientModifier(new AttributeModifier(
                        HEALTH_UUID, "Diamond Apple Hearts", 200, AttributeModifier.Operation.ADDITION));
            }
            player.setHealth(player.getMaxHealth());
            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_APPLE));
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
