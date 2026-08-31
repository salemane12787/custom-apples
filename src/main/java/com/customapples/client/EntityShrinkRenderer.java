package com.customapples.client;

import com.customapples.CustomApplesMod;
import com.customapples.event.EntityScaleHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CustomApplesMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityShrinkRenderer {
    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        float scale = resolveScale(entity);
        if (scale >= 1.0f) {
            return;
        }
        PoseStack pose = event.getPoseStack();
        pose.scale(scale, scale, scale);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!event.getEntity().level().isClientSide) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (ClientEntityScaleCache.has(entity.getId())) {
            entity.getPersistentData().putFloat(
                    EntityScaleHandler.SHRINK_KEY,
                    ClientEntityScaleCache.get(entity.getId(), 1.0f));
        }
    }

    private static float resolveScale(LivingEntity entity) {
        if (ClientEntityScaleCache.has(entity.getId())) {
            return ClientEntityScaleCache.get(entity.getId(), 1.0f);
        }
        if (entity.getPersistentData().contains(EntityScaleHandler.SHRINK_KEY)) {
            return entity.getPersistentData().getFloat(EntityScaleHandler.SHRINK_KEY);
        }
        return 1.0f;
    }
}
