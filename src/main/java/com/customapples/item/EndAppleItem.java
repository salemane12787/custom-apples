package com.customapples.item;

import com.customapples.progression.ProgressionManager;
import com.customapples.progression.UnlockStep;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.phys.AABB;

import java.util.Set;

public class EndAppleItem extends Item {
    public EndAppleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level.isClientSide || !(entity instanceof ServerPlayer serverPlayer)) {
            return super.finishUsingItem(stack, level, entity);
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return stack;
        }
        if (level.dimension() != Level.OVERWORLD) {
            teleportToEnd(serverPlayer, serverLevel);
        } else {
            buildEndPortal(serverLevel, serverPlayer.blockPosition());
            teleportToEnd(serverPlayer, serverLevel);
        }
        ProgressionManager.unlockStep(serverPlayer, UnlockStep.DIAMOND_APPLE_CHESTPLATE);
        return super.finishUsingItem(stack, level, entity);
    }

    private static void buildEndPortal(ServerLevel level, BlockPos near) {
        BlockPos base = near;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos p = base.offset(dx, 0, dz);
                if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                    Direction facing;
                    if (dx == -2) {
                        facing = Direction.EAST;
                    } else if (dx == 2) {
                        facing = Direction.WEST;
                    } else if (dz == -2) {
                        facing = Direction.SOUTH;
                    } else {
                        facing = Direction.NORTH;
                    }
                    level.setBlock(p, Blocks.END_PORTAL_FRAME.defaultBlockState()
                            .setValue(EndPortalFrameBlock.FACING, facing)
                            .setValue(EndPortalFrameBlock.HAS_EYE, true), 3);
                } else {
                    level.setBlock(p, Blocks.END_PORTAL.defaultBlockState(), 3);
                }
            }
        }
    }

    private static void teleportToEnd(ServerPlayer player, ServerLevel overworld) {
        ServerLevel endLevel = overworld.getServer().getLevel(Level.END);
        if (endLevel == null) {
            return;
        }
        BlockPos portal = endLevel.getSharedSpawnPos();
        player.teleportTo(endLevel, portal.getX(), portal.getY(), portal.getZ(),
                Set.of(), player.getYRot(), player.getXRot());
        for (EnderDragon dragon : endLevel.getEntitiesOfClass(EnderDragon.class,
                new AABB(portal).inflate(256))) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Ender Dragon located at " + dragon.blockPosition().toShortString()));
            break;
        }
    }
}
