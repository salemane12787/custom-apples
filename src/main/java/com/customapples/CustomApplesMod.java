package com.customapples;

import com.customapples.effect.ModMobEffects;
import com.customapples.entity.ModEntities;
import com.customapples.event.ModEvents;
import com.customapples.fluid.ModFluids;
import com.customapples.item.ModBlocks;
import com.customapples.item.ModCreativeTabs;
import com.customapples.item.ModItems;
import com.customapples.network.ModNetworking;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CustomApplesMod.MOD_ID)
public class CustomApplesMod {
    public static final String MOD_ID = "customapples";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CustomApplesMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modBus);
        ModBlocks.register(modBus);
        ModMobEffects.register(modBus);
        ModFluids.register(modBus);
        ModEntities.register(modBus);
        ModCreativeTabs.register(modBus);

        modBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(ModEvents.class);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetworking::register);
    }

    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
