package com.customapples.progression;

import com.customapples.item.ModBlocks;
import com.customapples.item.ModItems;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;
import java.util.Optional;

public enum UnlockStep implements StringRepresentable {
    LETTER_A("letter_a", ModItems.LETTER_A),
    APPLE_AXE("apple_axe", ModItems.APPLE_AXE),
    APPL("appl", ModItems.APPL),
    DIRT_APPLE("dirt_apple", ModItems.DIRT_APPLE),
    BREAD_APPLE("bread_apple", ModItems.BREAD_APPLE),
    APP("app", ModItems.APP),
    APPLE_BUCKET("apple_bucket", ModItems.APPLE_BUCKET),
    APPLE_SWORD("apple_sword", ModItems.APPLE_SWORD),
    IRON_APPLE("iron_apple", ModItems.IRON_APPLE),
    APPLE_BELL("apple_bell", ModBlocks.APPLE_BELL_ITEM),
    APPLE_APPLE_APPLE("apple_apple_apple", ModItems.APPLE_APPLE_APPLE),
    APPLE_BOOTS("apple_boots", ModItems.APPLE_BOOTS),
    APPLE_CHESTPLATE("apple_chestplate", ModItems.APPLE_CHESTPLATE),
    APPLE_FISHING_ROD("apple_fishing_rod", ModItems.APPLE_FISHING_ROD),
    EMERALD_APPLE("emerald_apple", ModItems.EMERALD_APPLE),
    APPLE_BLOCK("apple_block", ModBlocks.APPLE_BLOCK_ITEM),
    APPLE_LEGGINGS("apple_leggings", ModItems.APPLE_LEGGINGS),
    REDSTONE_APPLE("redstone_apple", ModItems.REDSTONE_APPLE),
    DIAMOND_APPLE("diamond_apple", ModItems.DIAMOND_APPLE),
    FLINT_AND_APPLE("flint_and_apple", ModItems.FLINT_AND_APPLE),
    APPLE_BOW("apple_bow", ModItems.APPLE_BOW),
    APPLETIZER("appletizer", ModItems.APPLETIZER),
    GOLDEN_APPLE_SWORD("golden_apple_sword", ModItems.GOLDEN_APPLE_SWORD),
    GOLDEN_APPLE_PICKAXE("golden_apple_pickaxe", ModItems.GOLDEN_APPLE_PICKAXE),
    GOLDEN_APPLE_BLOCK("golden_apple_block", ModBlocks.GOLDEN_APPLE_BLOCK_ITEM),
    SUPER_APPLE_SWORD("super_apple_sword", ModItems.SUPER_APPLE_SWORD),
    SUPER_APPLE_PICKAXE("super_apple_pickaxe", ModItems.SUPER_APPLE_PICKAXE),
    LAPIS_APPLE("lapis_apple", ModItems.LAPIS_APPLE),
    END_APPLE("end_apple", ModItems.END_APPLE),
    DIAMOND_APPLE_CHESTPLATE("diamond_apple_chestplate", ModItems.DIAMOND_APPLE_CHESTPLATE),
    DRAGON_APPLE("dragon_apple", ModItems.DRAGON_APPLE);

    public static final Codec<UnlockStep> CODEC = StringRepresentable.fromEnum(UnlockStep::values);

    private final String name;
    private final net.minecraftforge.registries.RegistryObject<Item> item;

    UnlockStep(String name, net.minecraftforge.registries.RegistryObject<Item> item) {
        this.name = name;
        this.item = item;
    }

    public int index() {
        return ordinal();
    }

    public Item getItem() {
        return item.get();
    }

    public ItemStack iconStack() {
        return new ItemStack(getItem());
    }

    public Component displayName() {
        return Component.translatable("unlock.customapples." + name);
    }

    public Optional<UnlockStep> next() {
        int n = ordinal() + 1;
        UnlockStep[] vals = values();
        return n < vals.length ? Optional.of(vals[n]) : Optional.empty();
    }

    public static Optional<UnlockStep> forItem(Item item) {
        for (UnlockStep step : values()) {
            if (step.getItem() == item) {
                return Optional.of(step);
            }
        }
        return Optional.empty();
    }

  public static String recipeHint(UnlockStep step) {
        return switch (step) {
            case LETTER_A -> "1 Apple";
            case APPLE_AXE -> "3 Apples + 2 Sticks";
            case APPL -> "2 Apples";
            case DIRT_APPLE -> "8 Dirt + 1 Apple";
            case BREAD_APPLE -> "2 Bread + 1 Apple";
            case APP -> "8 Iron Nuggets + 1 Apple";
            case APPLE_BUCKET -> "3 Apples";
            case APPLE_SWORD -> "2 Apples + 1 Iron";
            case IRON_APPLE -> "8 Iron + 1 Apple";
            case APPLE_BELL -> "6 Sticks + 1 Apple";
            case APPLE_APPLE_APPLE -> "3 Apples";
            case APPLE_BOOTS -> "2 Leather + 2 Apples";
            case APPLE_CHESTPLATE -> "Leather + Apples";
            case APPLE_FISHING_ROD -> "3 Sticks + String + Apple";
            case EMERALD_APPLE -> "8 Emeralds + 1 Apple";
            case APPLE_BLOCK -> "8 Apples + Obsidian";
            case APPLE_LEGGINGS -> "Apples + Gold";
            case REDSTONE_APPLE -> "8 Redstone + 1 Apple";
            case DIAMOND_APPLE -> "WARNING: Inventory";
            case FLINT_AND_APPLE -> "Flint + Apple";
            case APPLE_BOW -> "String + Apples";
            case APPLETIZER -> "8 Beef + 1 Apple";
            case GOLDEN_APPLE_SWORD -> "Gold Block + 2 Golden Apples";
            case GOLDEN_APPLE_PICKAXE -> "3 Golden Apples + 2 Blaze Rods";
            case GOLDEN_APPLE_BLOCK -> "Golden Apples + Nether Brick";
            case SUPER_APPLE_SWORD -> "Enchanted Golden Apple + Blaze Rod";
            case SUPER_APPLE_PICKAXE -> "Super Golden Apple + Blaze Rod";
            case LAPIS_APPLE -> "8 Lapis + 1 Apple";
            case END_APPLE -> "4 Pearls + 4 Blaze Rods + Apple";
            case DIAMOND_APPLE_CHESTPLATE -> "4 Diamonds + 4 Golden Apples";
            case DRAGON_APPLE -> "8 Apple Blocks + Dragon Egg";
        };
    }

    public boolean isGoldenTier() {
        return ordinal() >= GOLDEN_APPLE_SWORD.ordinal() && ordinal() <= SUPER_APPLE_PICKAXE.ordinal();
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static UnlockStep fromString(String s) {
        return UnlockStep.valueOf(s.toUpperCase(Locale.ROOT));
    }
}
