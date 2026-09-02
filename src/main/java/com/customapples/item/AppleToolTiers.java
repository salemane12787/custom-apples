package com.customapples.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.Tags;

/** Custom tiers with higher durability for golden and super apple tools. */
public final class AppleToolTiers {
    public static final Tier GOLDEN_SWORD = new ForgeTier(
            Tiers.IRON.getLevel(),
            AppleToolDurability.GOLDEN,
            Tiers.IRON.getSpeed(),
            Tiers.IRON.getAttackDamageBonus(),
            Tiers.IRON.getEnchantmentValue(),
            BlockTags.NEEDS_IRON_TOOL,
            () -> Ingredient.of(Items.GOLDEN_APPLE));

    public static final Tier GOLDEN_PICKAXE = new ForgeTier(
            Tiers.GOLD.getLevel(),
            AppleToolDurability.GOLDEN,
            Tiers.GOLD.getSpeed(),
            Tiers.GOLD.getAttackDamageBonus(),
            Tiers.GOLD.getEnchantmentValue(),
            Tags.Blocks.NEEDS_GOLD_TOOL,
            () -> Ingredient.of(Items.GOLDEN_APPLE));

    public static final Tier SUPER = new ForgeTier(
            Tiers.NETHERITE.getLevel(),
            AppleToolDurability.SUPER,
            Tiers.NETHERITE.getSpeed(),
            Tiers.NETHERITE.getAttackDamageBonus(),
            Tiers.NETHERITE.getEnchantmentValue(),
            Tags.Blocks.NEEDS_NETHERITE_TOOL,
            () -> Ingredient.of(Items.ENCHANTED_GOLDEN_APPLE));

    private AppleToolTiers() {}
}
