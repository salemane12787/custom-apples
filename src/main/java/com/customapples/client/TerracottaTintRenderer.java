package com.customapples.client;

import com.customapples.CustomApplesMod;
import com.customapples.util.TerracottaModeHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CustomApplesMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TerracottaTintRenderer {
    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        if (!TerracottaModeHelper.isTerracotta(event.getEntity())) {
            return;
        }
        RenderSystem.setShaderColor(0.75f, 0.45f, 0.28f, 1.0f);
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        if (!TerracottaModeHelper.isTerracotta(event.getEntity())) {
            return;
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
