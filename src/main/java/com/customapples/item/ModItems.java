package com.customapples.item;

import com.customapples.CustomApplesMod;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CustomApplesMod.MOD_ID);

    public static final RegistryObject<Item> WORM = ITEMS.register("worm",
            () -> new WormItem(new Item.Properties()));

    public static final RegistryObject<Item> LETTER_A = ITEMS.register("letter_a",
            () -> new LetterAItem(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(4).saturationMod(0.3f).fast().build())));

    public static final RegistryObject<Item> APPLE_AXE = ITEMS.register("apple_axe",
            () -> new AppleAxeItem(Tiers.WOOD, 9.0f, -3.1f, new Item.Properties()));

    public static final RegistryObject<Item> WOODEN_APPLE = ITEMS.register("wooden_apple",
            () -> new WoodenAppleItem(AppleFoodProperties.asFood()));

    public static final RegistryObject<Item> SPLINTER = ITEMS.register("splinter",
            () -> new SplinterItem(Tiers.WOOD, 7, -2.4f, new Item.Properties().durability(3)));

    public static final RegistryObject<Item> APPL = ITEMS.register("appl",
            () -> new ApplItem(AppleFoodProperties.asFood()));

    public static final RegistryObject<Item> DIRT_APPLE = ITEMS.register("dirt_apple",
            () -> new DirtAppleItem(AppleFoodProperties.asFood()));

    public static final RegistryObject<Item> BREAD_APPLE = ITEMS.register("bread_apple",
            () -> new BreadAppleItem(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(4).saturationMod(0.3f).alwaysEat().build())));

    public static final RegistryObject<Item> APP = ITEMS.register("app",
            () -> new AppItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> APPLE_BUCKET = ITEMS.register("apple_bucket",
            () -> new AppleBucketItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> APPLE_BUCKET_JUICE = ITEMS.register("apple_bucket_juice",
            () -> new AppleJuiceBucketItem(new Item.Properties().craftRemainder(APPLE_BUCKET.get()).stacksTo(1)));

    public static final RegistryObject<Item> APPLE_BUCKET_WATER = ITEMS.register("apple_bucket_water",
            () -> new AppleWaterBucketItem(new Item.Properties().craftRemainder(APPLE_BUCKET.get()).stacksTo(1)));

    public static final RegistryObject<Item> APPLE_SWORD = ITEMS.register("apple_sword",
            () -> new AppleSwordItem(Tiers.IRON, 1.0f, -2.4f, new Item.Properties()));

    public static final RegistryObject<Item> IRON_APPLE = ITEMS.register("iron_apple",
            () -> new IronAppleItem(new Item.Properties()));

    public static final RegistryObject<Item> APPLE_APPLE_APPLE = ITEMS.register("apple_apple_apple",
            () -> new AppleAppleAppleItem(new Item.Properties()));

    public static final RegistryObject<Item> APPLE_BOOTS = ITEMS.register("apple_boots",
            () -> new AppleArmorItem(AppleArmorMaterials.APPLE, ArmorItem.Type.BOOTS,
                    new Item.Properties(), 1));

    public static final RegistryObject<Item> APPLE_CHESTPLATE = ITEMS.register("apple_chestplate",
            () -> new AppleArmorItem(AppleArmorMaterials.APPLE, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties(), 2));

    public static final RegistryObject<Item> APPLE_LEGGINGS = ITEMS.register("apple_leggings",
            () -> new AppleArmorItem(AppleArmorMaterials.APPLE, ArmorItem.Type.LEGGINGS,
                    new Item.Properties(), 3));

    public static final RegistryObject<Item> REAL_APPLE = ITEMS.register("real_apple",
            () -> new RealAppleItem());

    public static final RegistryObject<Item> APPLE_FISHING_ROD = ITEMS.register("apple_fishing_rod",
            () -> new AppleFishingRodItem(new Item.Properties().durability(128)));

    public static final RegistryObject<Item> EMERALD_APPLE = ITEMS.register("emerald_apple",
            () -> new EmeraldAppleItem(AppleFoodProperties.asFood()));

    public static final RegistryObject<Item> REDSTONE_APPLE = ITEMS.register("redstone_apple",
            () -> new RedstoneAppleItem(new Item.Properties()));

    public static final RegistryObject<Item> FLINT_AND_APPLE = ITEMS.register("flint_and_apple",
            () -> new FlintAndAppleItem(new Item.Properties().durability(64)));

    public static final RegistryObject<Item> APPLE_BOW = ITEMS.register("apple_bow",
            () -> new AppleBowItem(new Item.Properties().durability(384)));

    public static final RegistryObject<Item> APPLETIZER = ITEMS.register("appletizer",
            () -> new AppletizerItem(AppleFoodProperties.asFood()));

    public static final RegistryObject<Item> DIAMOND_APPLE = ITEMS.register("diamond_apple",
            () -> new DiamondAppleItem(AppleFoodProperties.asFood()));

    public static final RegistryObject<Item> GOLDEN_APPLE_SWORD = ITEMS.register("golden_apple_sword",
            () -> new GoldenAppleSwordItem(new Item.Properties()));

    public static final RegistryObject<Item> GOLDEN_APPLE_PICKAXE = ITEMS.register("golden_apple_pickaxe",
            () -> new GoldenApplePickaxeItem(Tiers.GOLD, 1.0f, -2.8f, new Item.Properties()));

    public static final RegistryObject<Item> SUPER_APPLE_SWORD = ITEMS.register("super_apple_sword",
            () -> new SuperAppleSwordItem(new Item.Properties()));

    public static final RegistryObject<Item> SUPER_APPLE_PICKAXE = ITEMS.register("super_apple_pickaxe",
            () -> new SuperApplePickaxeItem(new Item.Properties()));

    public static final RegistryObject<Item> LAPIS_APPLE = ITEMS.register("lapis_apple",
            () -> new LapisAppleItem(AppleFoodProperties.asFood()));

    public static final RegistryObject<Item> END_APPLE = ITEMS.register("end_apple",
            () -> new EndAppleItem(AppleFoodProperties.asFood()));

    public static final RegistryObject<Item> DIAMOND_APPLE_CHESTPLATE = ITEMS.register("diamond_apple_chestplate",
            () -> new DiamondAppleChestplateItem(AppleFoodProperties.asFood()));

    public static final RegistryObject<Item> DRAGON_APPLE = ITEMS.register("dragon_apple",
            () -> new DragonAppleItem(AppleFoodProperties.asFood()));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
