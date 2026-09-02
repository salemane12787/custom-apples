package com.customapples.client;

import com.customapples.block.ModBlockEntities;
import com.customapples.entity.ModEntities;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EntityRenderersRegistration {
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AppleArmorModelLayers.APPLE_ARMOR_SET, AppleArmorModelLayers::appleArmorSet);
        event.registerLayerDefinition(WormModelLayers.WORM, WormModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new AppleArmorLayer(renderer, event.getEntityModels()));
            }
        }
        net.minecraft.client.renderer.entity.EntityRenderer<?> villagerRenderer =
                event.getRenderer(net.minecraft.world.entity.EntityType.VILLAGER);
        if (villagerRenderer instanceof net.minecraft.client.renderer.entity.VillagerRenderer villagerRendererTyped) {
            villagerRendererTyped.addLayer(new AppleVillagerLayer(villagerRendererTyped));
        }
    }

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiOverlaysEvent event) {
        ProgressionOverlay.register(event);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.APPLE_BELL.get(), AppleBellRenderer::new);

        event.registerEntityRenderer(ModEntities.THROWN_APPLE_BOMB.get(),
                ctx -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(ctx, 1.0f, true));
        event.registerEntityRenderer(ModEntities.THROWN_REDSTONE_APPLE.get(),
                ctx -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(ctx, 1.0f, true));

        event.registerEntityRenderer(ModEntities.WORM.get(), WormRenderer::new);
    }
}
