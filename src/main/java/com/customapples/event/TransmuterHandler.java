package com.customapples.event;

import com.customapples.item.AppleTransmuterBlock;
import com.customapples.item.GoldenAppleBlock;
import com.customapples.item.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = com.customapples.CustomApplesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TransmuterHandler {
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) {
            return;
        }
        Level level = event.level;
        if (level.getGameTime() % 5 != 0) {
            return;
        }

        for (ItemEntity item : level.getEntitiesOfClass(
                ItemEntity.class,
                new net.minecraft.world.phys.AABB(
                        -30000000,
                        level.getMinBuildHeight(),
                        -30000000,
                        30000000,
                        level.getMaxBuildHeight(),
                        30000000))) {
            BlockPos on = item.blockPosition();
            BlockPos below = on.below();
            if (level.getBlockState(on).getBlock() == ModBlocks.APPLE_BLOCK.get()
                    || level.getBlockState(below).getBlock() == ModBlocks.APPLE_BLOCK.get()) {
                BlockPos transmuter = level.getBlockState(on).getBlock() == ModBlocks.APPLE_BLOCK.get() ? on : below;
                AppleTransmuterBlock.transmuteItem(level, transmuter, item);
            } else if (level.getBlockState(on).getBlock() == ModBlocks.GOLDEN_APPLE_BLOCK.get()
                    || level.getBlockState(below).getBlock() == ModBlocks.GOLDEN_APPLE_BLOCK.get()) {
                BlockPos transmuter =
                        level.getBlockState(on).getBlock() == ModBlocks.GOLDEN_APPLE_BLOCK.get() ? on : below;
                GoldenAppleBlock.transmuteItem(level, transmuter, item);
            }
        }
    }
}
