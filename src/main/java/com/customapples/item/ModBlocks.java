package com.customapples.item;

import com.customapples.CustomApplesMod;
import com.customapples.fluid.ModFluids;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CustomApplesMod.MOD_ID);

    public static final RegistryObject<Block> APPLE_BLOCK = BLOCKS.register("apple_block",
            () -> new AppleTransmuterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.WOOD)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> APPLE_BELL = BLOCKS.register("apple_bell",
            () -> new AppleBellBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(2.0f)
                    .sound(SoundType.ANVIL)
                    .noOcclusion()));

    public static final RegistryObject<Block> GOLDEN_APPLE_BLOCK = BLOCKS.register("golden_apple_block",
            () -> new GoldenAppleBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .strength(3.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<LiquidBlock> APPLE_JUICE = BLOCKS.register("apple_juice",
            () -> new ModFluids.AppleJuiceLiquidBlock(ModFluids.APPLE_JUICE, ModFluids.appleJuiceBlockProperties()));

    public static final RegistryObject<Block> APPLE_WATER = BLOCKS.register("apple_water",
            () -> new ModFluids.AppleWaterBlock());

    public static final RegistryObject<Block> RED_APPLE_FIRE = BLOCKS.register("red_apple_fire",
            () -> new RedAppleFireBlock());

    public static final RegistryObject<Block> TERRACOTTA_STATUE = BLOCKS.register("terracotta_statue",
            () -> new TerracottaStatueBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_RED)
                    .strength(0.8f)
                    .sound(SoundType.DEEPSLATE)));

    public static final RegistryObject<Item> APPLE_BLOCK_ITEM = ModItems.ITEMS.register("apple_block",
            () -> new BlockItem(APPLE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> APPLE_BELL_ITEM = ModItems.ITEMS.register("apple_bell",
            () -> new BlockItem(APPLE_BELL.get(), new Item.Properties()));

    public static final RegistryObject<Item> GOLDEN_APPLE_BLOCK_ITEM = ModItems.ITEMS.register("golden_apple_block",
            () -> new BlockItem(GOLDEN_APPLE_BLOCK.get(), new Item.Properties()));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
