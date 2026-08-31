package com.customapples.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;

public class AppleFishingRodItem extends FishingRodItem {
    private static final int EXTRA_HOOKS = 2;

    public AppleFishingRodItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        boolean reeling = player.fishing != null;
        if (!level.isClientSide && reeling) {
            player.getPersistentData().putBoolean("CustomApplesRodReel", true);
        }
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (!level.isClientSide) {
            if (reeling) {
                reelAllHooks(level, player);
                player.getPersistentData().remove("CustomApplesRodReel");
            } else if (player.fishing != null) {
                castExtraHooks(level, player, hand);
            }
        }
        return result;
    }

    private static void castExtraHooks(Level level, Player player, InteractionHand hand) {
        ItemStack rod = player.getItemInHand(hand);
        int luck = net.minecraft.world.item.enchantment.EnchantmentHelper.getFishingLuckBonus(rod);
        int speed = net.minecraft.world.item.enchantment.EnchantmentHelper.getFishingSpeedBonus(rod);
        for (int i = 0; i < EXTRA_HOOKS; i++) {
            FishingHook hook = new FishingHook(player, level, luck, speed);
            double spread = i == 0 ? -0.85 : 0.85;
            double yawRad = player.getYRot() * (Math.PI / 180.0);
            double sideX = Math.cos(yawRad) * spread;
            double sideZ = Math.sin(yawRad) * spread;
            hook.moveTo(player.getX() + sideX, player.getY() + player.getEyeHeight() - 0.1, player.getZ() + sideZ);
            level.addFreshEntity(hook);
        }
    }

    private static void reelAllHooks(Level level, Player player) {
        for (FishingHook hook : level.getEntitiesOfClass(FishingHook.class, player.getBoundingBox().inflate(64))) {
            if (hook.getPlayerOwner() != player || hook.isRemoved()) {
                continue;
            }
            grantFishLoot(level, player);
            hook.discard();
        }
    }

    public static boolean canFishOnBlock(net.minecraft.world.level.block.state.BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL) || state.is(Blocks.ROOTED_DIRT);
    }

    public static void grantFishLoot(Level level, Player player) {
        if (level.isClientSide) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1));
        player.spawnAtLocation(new ItemStack(Items.APPLE, 2 + level.random.nextInt(3)), 0);
        player.spawnAtLocation(new ItemStack(Items.OAK_SAPLING, 1), 0);
        player.spawnAtLocation(new ItemStack(ModItems.WORM.get(), 1), 0);
    }
}
