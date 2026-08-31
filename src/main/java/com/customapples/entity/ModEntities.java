package com.customapples.entity;

import com.customapples.CustomApplesMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CustomApplesMod.MOD_ID);

    public static final RegistryObject<EntityType<WormEntity>> WORM = ENTITIES.register("worm",
            () -> EntityType.Builder.of(WormEntity::new, MobCategory.CREATURE)
                    .sized(0.4f, 0.2f)
                    .build("worm"));

    public static final RegistryObject<EntityType<ThrownAppleBombEntity>> THROWN_APPLE_BOMB =
            ENTITIES.register("thrown_apple_bomb",
                    () -> EntityType.Builder.<ThrownAppleBombEntity>of(ThrownAppleBombEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("thrown_apple_bomb"));

    public static final RegistryObject<EntityType<ThrownRedstoneAppleEntity>> THROWN_REDSTONE_APPLE =
            ENTITIES.register("thrown_redstone_apple",
                    () -> EntityType.Builder.<ThrownRedstoneAppleEntity>of(ThrownRedstoneAppleEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("thrown_redstone_apple"));

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}
