package com.customapples.event;

import com.customapples.item.ModItems;
import com.customapples.util.LootOnHitHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = com.customapples.CustomApplesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AppleBowArrowHandler {
  private static final String TARGET_KEY = "CustomApplesArrowTarget";
  private static final String TICK_KEY = "CustomApplesArrowTick";

    @SubscribeEvent
    public static void onArrowTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) {
            return;
        }
        if (event.level.getGameTime() % 5 != 0) {
            return;
        }
        for (AbstractArrow arrow : event.level.getEntitiesOfClass(AbstractArrow.class,
                new net.minecraft.world.phys.AABB(
                        -30000000, event.level.getMinBuildHeight(), -30000000,
                        30000000, event.level.getMaxBuildHeight(), 30000000))) {
            if (!arrow.getPersistentData().getBoolean("CustomApplesArrow")) {
                continue;
            }
            if (!arrow.getPersistentData().hasUUID(TARGET_KEY)) {
                continue;
            }
            UUID targetId = arrow.getPersistentData().getUUID(TARGET_KEY);
            LivingEntity target = null;
            for (LivingEntity living : event.level.getEntitiesOfClass(LivingEntity.class, arrow.getBoundingBox().inflate(64))) {
                if (living.getUUID().equals(targetId)) {
                    target = living;
                    break;
                }
            }
            if (target == null || !target.isAlive()) {
                arrow.discard();
                continue;
            }
            arrow.setNoPhysics(true);
            arrow.setPos(target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ());
            arrow.setDeltaMovement(0, 0, 0);
            int tick = arrow.getPersistentData().getInt(TICK_KEY);
            arrow.getPersistentData().putInt(TICK_KEY, tick + 1);
            if (tick % 2 != 0) {
                continue;
            }
            if (arrow.getOwner() instanceof Player player) {
                target.hurt(player.damageSources().playerAttack(player), 3.0f);
                LootOnHitHelper.dropHitLoot(player, target, 1);
                target.level().playSound(null, target.blockPosition(), SoundEvents.GENERIC_EAT,
                        SoundSource.PLAYERS, 0.8f, 0.9f);
            }
        }
    }
}
