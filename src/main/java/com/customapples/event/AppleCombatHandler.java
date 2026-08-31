package com.customapples.event;

import com.customapples.item.*;
import com.customapples.util.LootOnHitHelper;
import com.customapples.util.TerracottaModeHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class AppleCombatHandler {
    private AppleCombatHandler() {}

    public static boolean isAppleSword(ItemStack stack) {
        return stack.is(ModItems.APPLE_SWORD.get())
                || stack.is(ModItems.GOLDEN_APPLE_SWORD.get())
                || stack.is(ModItems.SUPER_APPLE_SWORD.get());
    }

    public static int lootMultiplier(ItemStack stack) {
        if (stack.is(ModItems.SUPER_APPLE_SWORD.get())) return 8;
        if (stack.is(ModItems.GOLDEN_APPLE_SWORD.get())) return 4;
        return 1;
    }

    public static float bonusDamage(ItemStack stack) {
        if (stack.is(ModItems.SUPER_APPLE_SWORD.get())) return 12f;
        if (stack.is(ModItems.GOLDEN_APPLE_SWORD.get())) return 6f;
        return 4f;
    }

    public static void onAttack(Player player, LivingEntity target) {
        ItemStack weapon = player.getMainHandItem();
        if (!isAppleSword(weapon)) return;
        Level level = player.level();
        if (level.isClientSide) return;

        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EAT,
                SoundSource.PLAYERS, 1.0f, 0.8f);
        target.hurt(player.damageSources().playerAttack(player), bonusDamage(weapon));
        player.getFoodData().eat(3, 0.6f);
        LootOnHitHelper.dropHitLoot(player, target, 2);

        if (target instanceof EnderDragon dragon) {
            float scale = dragon.getPersistentData().getFloat("CustomApplesDragonScale");
            scale = Math.max(0.3f, scale - 0.08f);
            dragon.getPersistentData().putFloat("CustomApplesDragonScale", scale);
            dragon.refreshDimensions();
        } else {
            EntityScaleHandler.shrink(target, 0.06f);
        }
    }

    public static void onHurt(Player player, LivingEntity target, net.minecraftforge.event.entity.living.LivingHurtEvent event) {
        if (!isAppleSword(player.getMainHandItem())) return;
        if (TerracottaModeHelper.isTerracotta(target)) {
            event.setAmount(999f);
        }
    }

    public static void onKill(Player player, LivingEntity entity) {
        ItemStack weapon = player.getMainHandItem();
        if (!isAppleSword(weapon)) return;
        Level level = entity.level();
        int apples = 3 + level.random.nextInt(3);
        entity.spawnAtLocation(new ItemStack(Items.APPLE, apples));
        LootOnHitHelper.dropHitLoot(player, entity, lootMultiplier(weapon));
    }
}
