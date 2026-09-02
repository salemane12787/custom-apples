package com.customapples.client;

import com.customapples.CustomApplesMod;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = CustomApplesMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ItemHintTooltipHandler {
    private ItemHintTooltipHandler() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null || !CustomApplesMod.MOD_ID.equals(id.getNamespace())) {
            return;
        }
        String key = "item." + id.getNamespace() + "." + id.getPath() + ".tooltip";
        if (I18n.exists(key)) {
            event.getToolTip().add(Component.translatable(key));
        }
    }
}
