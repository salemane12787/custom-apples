package com.customapples.item;

import com.customapples.util.TerrainDestroyer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class DiamondAppleItem extends Item {
    public DiamondAppleItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.customapples.diamond_apple.warning")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            TerrainDestroyer.transmuteToDiamonds(level, player, 16);
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
