package com.customapples.item;

import com.customapples.event.AppletizerWaveHandler;
import com.customapples.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AppletizerItem extends Item {
    private static final String FACING_KEY = "CustomApplesAppletizerFacing";

    public AppletizerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            Direction facing = Direction.getNearest(
                    player.getLookAngle().x, 0, player.getLookAngle().z);
            player.getPersistentData().putInt(FACING_KEY, facing.get3DDataValue());
        }
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            Direction facing = Direction.from3DDataValue(
                    player.getPersistentData().getInt(FACING_KEY));
            if (facing == null) {
                facing = player.getDirection();
            }

            BlockPos origin = player.blockPosition();
            level.playSound(null, origin, SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1.2f, 0.5f);
            if (level instanceof ServerLevel serverLevel) {
                ServerPlayer serverPlayer = player instanceof ServerPlayer sp ? sp : null;
                if (serverPlayer != null) {
                    ModNetworking.sendFlash(serverPlayer, 14);
                    ModNetworking.sendShake(serverPlayer, 0.55f);
                }
                AppletizerWaveHandler.start(serverLevel, origin, facing);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                return stack;
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
