package com.customapples.event;

import com.customapples.CustomApplesMod;
import com.customapples.network.ModNetworking;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CustomApplesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityScaleHandler {
    public static final String SHRINK_KEY = "CustomApplesShrinkScale";

    @SubscribeEvent
    public static void onEntitySize(EntityEvent.Size event) {
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        if (!living.getPersistentData().contains(SHRINK_KEY)) {
            return;
        }
        float scale = living.getPersistentData().getFloat(SHRINK_KEY);
        if (scale > 0f && scale < 1f) {
            event.setNewSize(event.getNewSize().scale(scale));
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (!event.getEntity().level().isClientSide) {
            ModNetworking.sendEntityScaleClear(event.getEntity());
        }
    }

    public static void shrink(LivingEntity target, float amount) {
        float scale = target.getPersistentData().getFloat(SHRINK_KEY);
        if (scale <= 0f) {
            scale = 1.0f;
        }
        scale = Math.max(0.25f, scale - amount);
        applyScale(target, scale);
    }

    public static void setScale(LivingEntity target, float scale) {
        applyScale(target, Math.max(0.2f, Math.min(1.0f, scale)));
    }

    private static void applyScale(LivingEntity target, float scale) {
        target.getPersistentData().putFloat(SHRINK_KEY, scale);
        target.refreshDimensions();
        if (!target.level().isClientSide) {
            ModNetworking.sendEntityScale(target, scale);
        }
    }
}
