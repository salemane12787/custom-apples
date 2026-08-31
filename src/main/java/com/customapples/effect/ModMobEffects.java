package com.customapples.effect;

import com.customapples.CustomApplesMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, CustomApplesMod.MOD_ID);

    /** Bread Apple — eat any non-mod item for a short time. */
    public static final RegistryObject<MobEffect> OMNIVORE =
            MOB_EFFECTS.register("omnivore", () -> new SimpleMobEffect(MobEffectCategory.BENEFICIAL, 0xC84A2D));

    public static final RegistryObject<MobEffect> WIDE =
            MOB_EFFECTS.register("wide", () -> new SimpleMobEffect(MobEffectCategory.BENEFICIAL, 0x55AAFF));

    public static void register(IEventBus bus) {
        MOB_EFFECTS.register(bus);
    }
}
