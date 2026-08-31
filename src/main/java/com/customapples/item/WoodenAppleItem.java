package com.customapples.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WoodenAppleItem extends Item {
    public WoodenAppleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player && !level.isClientSide) {
            player.setHealth(1.0f);
            if (!player.getInventory().add(new ItemStack(ModItems.SPLINTER.get()))) {
                player.drop(new ItemStack(ModItems.SPLINTER.get()), false);
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
