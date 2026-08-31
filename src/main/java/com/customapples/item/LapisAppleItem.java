package com.customapples.item;

import com.customapples.progression.ProgressionManager;
import com.customapples.util.EnchantHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LapisAppleItem extends Item {
    public LapisAppleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            EnchantHelper.maxEnchantInventory(player);
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack slot = player.getInventory().getItem(i);
                if (slot.is(net.minecraft.world.item.Items.GOLDEN_APPLE)) {
                    player.getInventory().setItem(
                            i,
                            new ItemStack(net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE, slot.getCount()));
                }
            }
            ProgressionManager.unlockForItem(player, ModItems.LAPIS_APPLE.get());
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
