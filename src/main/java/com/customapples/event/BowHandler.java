package com.customapples.event;

import com.customapples.item.ModItems;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = com.customapples.CustomApplesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BowHandler {
    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        ItemStack bow = event.getBow();
        if (!bow.is(ModItems.APPLE_BOW.get())) return;
        if (!event.getEntity().level().isClientSide) {
            event.getEntity().getPersistentData().putBoolean("CustomApplesAppleBowShot", true);
        }
    }

    @SubscribeEvent
    public static void onArrowSpawn(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof AbstractArrow arrow && !event.getLevel().isClientSide) {
            if (arrow.getOwner() instanceof net.minecraft.world.entity.player.Player player) {
                ItemStack bow = player.getMainHandItem();
                if (!bow.is(ModItems.APPLE_BOW.get())) {
                    bow = player.getOffhandItem();
                }
                if (bow.is(ModItems.APPLE_BOW.get())
                        || player.getPersistentData().getBoolean("CustomApplesAppleBowShot")) {
                    arrow.getPersistentData().putBoolean("CustomApplesArrow", true);
                    player.getPersistentData().remove("CustomApplesAppleBowShot");
                }
            }
        }
    }
}
