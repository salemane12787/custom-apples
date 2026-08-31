package com.customapples.client;

import com.customapples.CustomApplesMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CustomApplesMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientFlashHandler {
    private static int flashTicks = 0;

    public static void trigger(int ticks) {
        flashTicks = Math.max(flashTicks, ticks);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (flashTicks <= 0) {
            return;
        }
        int alpha = Math.min(220, flashTicks * 18);
        event.getGuiGraphics().fill(0, 0, event.getWindow().getGuiScaledWidth(),
                event.getWindow().getGuiScaledHeight(), (alpha << 24) | 0xFFFFFF);
        flashTicks--;
    }
}
