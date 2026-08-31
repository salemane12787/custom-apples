package com.customapples.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

/** Client-side apple juice fluid appearance using vanilla water textures with an orange tint. */
public final class ModFluidClient {
    /** Warm amber-orange apple juice tint on vanilla water textures. */
    public static final int APPLE_JUICE_COLOR = 0xFFC87828;
    private static final ResourceLocation WATER_STILL =
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_still");
    private static final ResourceLocation WATER_FLOW =
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_flow");

    public static final IClientFluidTypeExtensions APPLE_JUICE_EXTENSIONS = new IClientFluidTypeExtensions() {
        @Override
        public ResourceLocation getStillTexture() {
            return WATER_STILL;
        }

        @Override
        public ResourceLocation getFlowingTexture() {
            return WATER_FLOW;
        }

        @Override
        public int getTintColor() {
            return APPLE_JUICE_COLOR;
        }
    };

    private ModFluidClient() {
    }
}
