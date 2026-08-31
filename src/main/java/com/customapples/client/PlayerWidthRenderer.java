package com.customapples.client;

import com.customapples.CustomApplesMod;
import com.customapples.effect.ModMobEffects;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CustomApplesMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerWidthRenderer {
    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) {
            return;
        }
        if (!player.hasEffect(ModMobEffects.WIDE.get())) {
            return;
        }
        event.getPoseStack().scale(2.5f, 1.0f, 2.5f);
    }
}
