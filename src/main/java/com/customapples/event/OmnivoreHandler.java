package com.customapples.event;

import com.customapples.CustomApplesMod;
import com.customapples.item.BreadAppleItem;
import com.customapples.item.ModItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Omnivore only eats plain vanilla non-food items (dirt, sticks, etc.).
 * All mod apples/items keep their normal right-click / eat behaviour.
 */
@Mod.EventBusSubscriber(modid = CustomApplesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class OmnivoreHandler {
    public static final int EAT_DURATION = 32;

    public static boolean isOmnivoreCandidate(ItemStack stack) {
        if (stack.isEmpty() || stack.is(ModItems.BREAD_APPLE.get())) {
            return false;
        }
        if (stack.getItem().isEdible()) {
            return false;
        }
        if (stack.getUseAnimation() != UseAnim.NONE || stack.getUseDuration() > 0) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && !id.getNamespace().equals(CustomApplesMod.MOD_ID);
    }

    private static boolean isOmnivoreEating(Player player, ItemStack stack) {
        return BreadAppleItem.isOmnivoreActive(player) && isOmnivoreCandidate(stack);
    }

    @SubscribeEvent
    public static void onStartUse(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack stack = event.getItem();
        if (isOmnivoreEating(player, stack)) {
            event.setDuration(EAT_DURATION);
        }
    }

    @SubscribeEvent
    public static void onTickUse(LivingEntityUseItemEvent.Tick event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack stack = event.getItem();
        if (!isOmnivoreEating(player, stack)) {
            return;
        }
        if (!shouldTriggerEatEffects(player)) {
            return;
        }
        if (!player.level().isClientSide) {
            triggerEatEffects(player, stack, 5);
        }
    }

    @SubscribeEvent
    public static void onFinishUse(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack stack = event.getItem();
        if (!isOmnivoreEating(player, stack)) {
            return;
        }
        if (!player.level().isClientSide) {
            int nutrition = Math.max(1, stack.getMaxDamage() > 0 ? 2 : 1);
            player.getFoodData().eat(nutrition, 0.3f);
            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.PLAYER_BURP,
                    SoundSource.PLAYERS,
                    0.5f,
                    player.getRandom().nextFloat() * 0.1f + 0.9f);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        event.setResultStack(stack);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (!BreadAppleItem.isOmnivoreActive(player)) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (!isOmnivoreCandidate(stack)) {
            return;
        }
        InteractionHand hand = event.getHand();
        if (!player.isUsingItem()) {
            player.startUsingItem(hand);
        }
        event.setCancellationResult(InteractionResultHolder.consume(stack).getResult());
        event.setCanceled(true);
    }

    static boolean shouldTriggerEatEffects(Player player) {
        int elapsed = player.getTicksUsingItem();
        return elapsed > 0 && elapsed % 4 == 0;
    }

    static void triggerEatEffects(Player player, ItemStack stack, int particleCount) {
        RandomSource random = player.getRandom();
        float pitch = 0.5f + random.nextFloat() * 0.4f;
        player.playSound(SoundEvents.GENERIC_EAT, 0.5f, pitch);
        spawnEatParticles(player, stack, particleCount);
    }

    static void spawnEatParticles(Player player, ItemStack stack, int count) {
        RandomSource random = player.getRandom();
        for (int i = 0; i < count; ++i) {
            Vec3 velocity = new Vec3(
                    (random.nextFloat() - 0.5D) * 0.1D,
                    Math.random() * 0.1D + 0.1D,
                    0.0D);
            velocity = velocity.xRot(-player.getXRot() * Mth.DEG_TO_RAD);
            velocity = velocity.yRot(-player.getYRot() * Mth.DEG_TO_RAD);

            Vec3 position = new Vec3(
                    (random.nextFloat() - 0.5D) * 0.3D,
                    -random.nextFloat() * 0.6D - 0.3D,
                    0.6D);
            position = position.xRot(-player.getXRot() * Mth.DEG_TO_RAD);
            position = position.yRot(-player.getYRot() * Mth.DEG_TO_RAD);
            position = position.add(player.getX(), player.getEyeY(), player.getZ());

            ItemParticleOption particle = new ItemParticleOption(ParticleTypes.ITEM, stack);
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        particle,
                        position.x,
                        position.y,
                        position.z,
                        1,
                        velocity.x,
                        velocity.y + 0.05D,
                        velocity.z,
                        0.0D);
            }
        }
    }

}
