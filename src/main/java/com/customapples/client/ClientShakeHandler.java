package com.customapples.client;

import com.customapples.CustomApplesMod;
import com.customapples.network.ModNetworking;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;

public class ClientShakeHandler {
    private static float shakeIntensity = 0f;

    public static float getShakeIntensity() {
        return shakeIntensity;
    }

    public static void trigger(float intensity) {
        shakeIntensity = Math.max(shakeIntensity, intensity);
    }

    @Mod.EventBusSubscriber(modid = CustomApplesMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientTicker {
        @SubscribeEvent
        public static void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
            if (event.phase == net.minecraftforge.event.TickEvent.Phase.END && shakeIntensity > 0) {
                shakeIntensity = Math.max(0, shakeIntensity - 0.05f);
            }
        }

        @SubscribeEvent
        public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
            if (shakeIntensity <= 0) {
                return;
            }
            float amp = shakeIntensity * 4f;
            event.setYaw(event.getYaw() + (float) (Math.sin(event.getPartialTick() * 20) * amp));
            event.setPitch(event.getPitch() + (float) (Math.cos(event.getPartialTick() * 17) * amp));
        }
    }

    public static void handleShakePacket(ModNetworking.ShakePacket pkt, NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> trigger(pkt.intensity()));
        ctx.setPacketHandled(true);
    }
}
