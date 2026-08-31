package com.customapples.item;

import com.customapples.network.ModNetworking;
import com.customapples.progression.ProgressionManager;
import com.customapples.progression.UnlockStep;
import com.customapples.util.StructurePlacer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.Optional;

public class DragonAppleItem extends Item {
    public DragonAppleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level.isClientSide || !(entity instanceof ServerPlayer serverPlayer)) {
            return super.finishUsingItem(stack, level, entity);
        }
        if (level.dimension() != Level.END) {
            serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Dragon Apple only works in the End!"));
            return stack;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return stack;
        }
        boolean dragonDead = serverLevel.getEntitiesOfClass(EnderDragon.class,
                serverPlayer.getBoundingBox().inflate(512)).isEmpty();
        if (!dragonDead) {
            serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Defeat the Ender Dragon first!"));
            return stack;
        }
        Optional<StructurePlacer.BigTreePlacement> placement =
                StructurePlacer.placeBigTreeFarFrom(serverLevel, serverPlayer.blockPosition());
        if (placement.isEmpty()) {
            serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Could not place the Big Tree — no valid ground found far enough away."));
            return stack;
        }
        BlockPos chestPos = placement.get().chestPos();
        BlockPos treeCenter = placement.get().treeCenter();
        Direction chestFacing = placement.get().chestFacing();
        serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "Big Tree spawned at " + treeCenter.getX() + ", " + treeCenter.getY() + ", " + treeCenter.getZ()));
        serverLevel.setBlock(
                chestPos,
                Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, chestFacing),
                3);
        BlockEntity be = serverLevel.getBlockEntity(chestPos);
        if (be instanceof ChestBlockEntity chest) {
            chest.setItem(12, new ItemStack(Items.GOLDEN_APPLE, 8));
            chest.setItem(13, new ItemStack(ModItems.REAL_APPLE.get(), 1));
        }
        ModNetworking.sendShake(serverPlayer, 2.0f);
        ProgressionManager.unlockStep(serverPlayer, UnlockStep.DRAGON_APPLE);
        return super.finishUsingItem(stack, level, entity);
    }
}
