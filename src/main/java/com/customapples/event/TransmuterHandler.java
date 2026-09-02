package com.customapples.event;

import com.customapples.item.AppleTransmuterBlock;
import com.customapples.item.GoldenAppleBlock;
import com.customapples.item.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = com.customapples.CustomApplesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TransmuterHandler {
    private static final double SCAN_RADIUS = 64.0;

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) {
            return;
        }
        Level level = event.level;
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (level.getGameTime() % 10 != 0) {
            return;
        }

        for (ServerPlayer player : serverLevel.players()) {
            AABB box = player.getBoundingBox().inflate(SCAN_RADIUS);
            for (ItemEntity item : serverLevel.getEntitiesOfClass(ItemEntity.class, box)) {
                processItemOnTransmuter(level, item);
            }
        }
    }

    private static void processItemOnTransmuter(Level level, ItemEntity item) {
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
