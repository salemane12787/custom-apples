package com.customapples.event;

import com.customapples.item.*;
import com.customapples.progression.ProgressionManager;
import com.customapples.util.RecipeUnlocker;
import com.customapples.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = com.customapples.CustomApplesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        ItemStack tool = player.getMainHandItem();

        if (tool.is(ModItems.APPLE_AXE.get()) && TreeCapitatorHelper.isLeaves(event.getState())) {
            event.setCanceled(true);
            return;
        }

        if (tool.is(ModItems.APPLE_AXE.get()) && TreeCapitatorHelper.isLog(event.getState())) {
            event.setCanceled(true);
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                TreeCapitatorHelper.breakTree(serverLevel, event.getPos(), player, tool);
            }
            return;
        }

        if (event.getLevel() instanceof Level level) {
            if (BlockBreakGuard.isProtected(event.getState())) {
                return;
            }
            if (tool.is(ModItems.GOLDEN_APPLE_PICKAXE.get())) {
                GoldenApplePickaxeItem.onBlockMined(level, event.getPos(), event.getState());
            }
            if (tool.is(ModItems.SUPER_APPLE_PICKAXE.get())) {
                if (event.getLevel() instanceof ServerLevel serverLevel) {
                    SuperApplePickaxeItem.onBlockMined(serverLevel, event.getPos(), player, tool);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!AppleCombatHandler.isAppleSword(player.getMainHandItem())) return;
        LivingEntity target = event.getTarget() instanceof LivingEntity le ? le : null;
        if (target == null) return;
        if (!player.level().isClientSide) {
            AppleCombatHandler.onAttack(player, target);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            AppleCombatHandler.onHurt(player, event.getEntity(), event);
        }
    }

    @SubscribeEvent
    public static void onEnderTeleport(EntityTeleportEvent.EnderEntity event) {
        if (event.getEntity().getPersistentData().getBoolean("CustomApplesNoTeleport")) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            AppleCombatHandler.onKill(player, event.getEntity());
        }
        RedAppleFireHelper.onMobDeath(event.getEntity().level(), event.getEntity());
    }

    @SubscribeEvent
    public static void onLeftClickBlock(net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Player player = event.getEntity();
        if (!AppleCombatHandler.isAppleSword(player.getMainHandItem())) {
            return;
        }
        BlockPos pos = event.getPos();
        if (TerracottaModeHelper.isStatueBlock(event.getLevel(), pos)) {
            event.getLevel().destroyBlock(pos, false);
            TerracottaStatueBlock.dropAppleBurst(event.getLevel(), pos);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingFall(net.minecraftforge.event.entity.living.LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            BlockPos below = player.blockPosition().below();
            if (player.level().getBlockState(below).getBlock() == ModBlocks.APPLE_WATER.get()
                    || player.level().getBlockState(player.blockPosition()).getBlock() == ModBlocks.APPLE_WATER.get()) {
                event.setCanceled(true);
                player.resetFallDistance();
            }
        }
    }

    @SubscribeEvent
    public static void onSplinterBreak(PlayerDestroyItemEvent event) {
        // Splinter breaks like a normal low-durability sword — no explosion.
    }

    @SubscribeEvent
    public static void onInteractVillager(net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof net.minecraft.world.entity.npc.Villager villager
                && villager.getTags().contains(com.customapples.util.AppleVillagerHelper.APPLE_VILLAGER_TAG)
                && !event.getEntity().level().isClientSide) {
            Player player = event.getEntity();
            player.getFoodData().eat(8, 0.8f);
            player.level().playSound(null, villager.blockPosition(), SoundEvents.GENERIC_EAT,
                    SoundSource.PLAYERS, 1.0f, 0.8f);
            villager.spawnAtLocation(new ItemStack(Items.APPLE, 4));
            villager.discard();
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    public static void onItemCraft(PlayerEvent.ItemCraftedEvent event) {
        ProgressionManager.onCraft(event.getEntity(), event.getCrafting());
    }

    @SubscribeEvent
    public static void onProjectileImpact(net.minecraftforge.event.entity.ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof AbstractArrow arrow
                && arrow.getPersistentData().getBoolean("CustomApplesArrow")) {
            if (event.getRayTraceResult().getType() == net.minecraft.world.phys.HitResult.Type.ENTITY) {
                var entityHit = (net.minecraft.world.phys.EntityHitResult) event.getRayTraceResult();
                if (entityHit.getEntity() instanceof LivingEntity living) {
                    arrow.getPersistentData().putUUID("CustomApplesArrowTarget", living.getUUID());
                    arrow.setNoPhysics(true);
                    arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                    arrow.setPierceLevel((byte) 127);
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().getPersistentData().getInt("CustomApplesUnlockIndex") == 0
                && !event.getEntity().getPersistentData().contains("CustomApplesInit")) {
            event.getEntity().getPersistentData().putBoolean("CustomApplesInit", true);
            ProgressionManager.unlockStep(event.getEntity(), com.customapples.progression.UnlockStep.LETTER_A);
            RecipeUnlocker.sync(event.getEntity());
        }
    }
}
