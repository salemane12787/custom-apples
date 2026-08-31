package com.customapples.item;

import com.customapples.CustomApplesMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CustomApplesMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> CUSTOM_APPLES_TAB = CREATIVE_TABS.register("custom_apples",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.customapples"))
                    .icon(() -> new ItemStack(ModItems.APPLE_AXE.get()))
                    .displayItems((params, output) -> {
                        ModItems.ITEMS.getEntries().forEach(item -> {
                            ItemStack stack = new ItemStack(item.get());
                            if (!stack.isEmpty()) {
                                output.accept(stack);
                            }
                        });
                    })
                    .build());

    public static void register(IEventBus bus) {
        CREATIVE_TABS.register(bus);
    }
}
