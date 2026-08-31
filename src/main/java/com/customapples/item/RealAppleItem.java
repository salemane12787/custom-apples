package com.customapples.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** Edible item rendered with the photoscan 3D mesh. Eating it removes you from the world. */
public class RealAppleItem extends Item {
    public RealAppleItem() {
        super(new Properties().food(
                new FoodProperties.Builder().nutrition(4).saturationMod(0.3f).alwaysEat().build()));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);
        if (!level.isClientSide && livingEntity instanceof ServerPlayer player) {
            player.connection.disconnect(Component.translatable("disconnect.customapples.real_apple"));
        }
        return result;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return com.customapples.client.RealAppleItemRenderer.getInstance();
            }
        });
    }
}
