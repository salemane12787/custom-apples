package com.customapples.event;

import com.customapples.item.AppleFishingRodItem;
import com.customapples.item.ModItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = com.customapples.CustomApplesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FishingRodHandler {
    private static final String GROUND_WAIT = "CustomApplesGroundFishWait";

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }
        Player player = event.player;
        if (!hasAppleRod(player)) {
            return;
        }
        for (FishingHook hook : player.level().getEntitiesOfClass(
                FishingHook.class, player.getBoundingBox().inflate(32))) {
            if (hook.getPlayerOwner() != player) {
                continue;
            }
            processGroundHook(hook, player);
        }
    }

    private static void processGroundHook(FishingHook hook, Player player) {
        if (hook.isInWater()) {
            return;
        }
        BlockPos below = hook.blockPosition().below();
        BlockState ground = hook.level().getBlockState(below);
        BlockState at = hook.level().getBlockState(hook.blockPosition());
        if (!AppleFishingRodItem.canFishOnBlock(ground) && !AppleFishingRodItem.canFishOnBlock(at)) {
            return;
        }
        if (!hook.onGround()) {
            hook.getPersistentData().putInt(GROUND_WAIT, 0);
            return;
        }
        int wait = hook.getPersistentData().getInt(GROUND_WAIT) + 1;
        hook.getPersistentData().putInt(GROUND_WAIT, wait);
        if (wait < 12) {
            return;
        }
        if (hook.level().random.nextInt(20) != 0) {
            return;
        }
        hook.getPersistentData().putInt(GROUND_WAIT, 0);
        AppleFishingRodItem.grantFishLoot(hook.level(), player);
        hook.spawnAtLocation(new ItemStack(Items.APPLE, 2));
        hook.level().playSound(null, hook.blockPosition(), SoundEvents.FISHING_BOBBER_RETRIEVE,
                player.getSoundSource(), 1.0f, 1.0f);
        hook.discard();
    }

    private static boolean hasAppleRod(Player player) {
        return player.getMainHandItem().is(ModItems.APPLE_FISHING_ROD.get())
                || player.getOffhandItem().is(ModItems.APPLE_FISHING_ROD.get());
    }

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        Player player = event.getEntity();
        ItemStack rod = player.getMainHandItem();
        if (!rod.is(ModItems.APPLE_FISHING_ROD.get())) {
            rod = player.getOffhandItem();
        }
        if (!rod.is(ModItems.APPLE_FISHING_ROD.get())) {
            return;
        }

        event.getDrops().clear();
        if (!player.level().isClientSide && !player.getPersistentData().getBoolean("CustomApplesRodReel")) {
            event.getDrops().add(new ItemStack(Items.APPLE, 2 + player.getRandom().nextInt(3)));
            event.getDrops().add(new ItemStack(Items.OAK_SAPLING, 1));
            event.getDrops().add(new ItemStack(ModItems.WORM.get(), 1));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1));
        }
    }

    public static boolean canHookOnGround(FishingHook hook) {
        if (hook.getPlayerOwner() == null) return false;
        Player owner = hook.getPlayerOwner();
        ItemStack rod = owner.getMainHandItem();
        if (!rod.is(ModItems.APPLE_FISHING_ROD.get())) {
            rod = owner.getOffhandItem();
        }
        if (!rod.is(ModItems.APPLE_FISHING_ROD.get())) return false;
        return AppleFishingRodItem.canFishOnBlock(hook.level().getBlockState(hook.blockPosition()));
    }
}
