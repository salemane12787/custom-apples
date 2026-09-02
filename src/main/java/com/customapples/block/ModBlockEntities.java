package com.customapples.block;

import com.customapples.CustomApplesMod;
import com.customapples.item.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CustomApplesMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<BellBlockEntity>> APPLE_BELL =
            BLOCK_ENTITIES.register("apple_bell",
                    () -> BlockEntityType.Builder.of(BellBlockEntity::new, ModBlocks.APPLE_BELL.get()).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
