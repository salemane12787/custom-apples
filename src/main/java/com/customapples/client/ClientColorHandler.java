package com.customapples.client;

import com.customapples.CustomApplesMod;
import com.customapples.item.ModBlocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CustomApplesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientColorHandler {
    /** Warm amber-brown apple juice tint on water-style texture. */
    private static final int APPLE_JUICE_COLOR = ModFluidClient.APPLE_JUICE_COLOR;
    /** Red-shifted fire tint: full red, reduced green/blue for apple-fire look. */
    private static final int RED_APPLE_FIRE_COLOR = 0xFF5533;

    private ClientColorHandler() {
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> APPLE_JUICE_COLOR, ModBlocks.APPLE_JUICE.get());
        event.register((state, level, pos, tintIndex) -> RED_APPLE_FIRE_COLOR, ModBlocks.RED_APPLE_FIRE.get());
    }
}
