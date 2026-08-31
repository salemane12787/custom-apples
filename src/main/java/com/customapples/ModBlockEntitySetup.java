package com.customapples;

import com.customapples.item.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = CustomApplesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBlockEntitySetup {
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModBlockEntitySetup::registerAppleBellOnBellType);
    }

    /**
     * Vanilla BlockEntityType.BELL only lists minecraft:bell.
     * Without this, apple_bell gets "invalid for ticking" and the swinging bell body never renders.
     */
    private static void registerAppleBellOnBellType() {
        try {
            Field validBlocksField = BlockEntityType.class.getDeclaredField("validBlocks");
            validBlocksField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Set<Block> current = (Set<Block>) validBlocksField.get(BlockEntityType.BELL);
            if (current.contains(ModBlocks.APPLE_BELL.get())) {
                return;
            }

            Set<Block> expanded = new HashSet<>(current);
            expanded.add(ModBlocks.APPLE_BELL.get());

            Unsafe unsafe = getUnsafe();
            long offset = unsafe.objectFieldOffset(validBlocksField);
            unsafe.putObject(BlockEntityType.BELL, offset, expanded);

            CustomApplesMod.LOGGER.info("Registered apple_bell on BlockEntityType.BELL ({} blocks)", expanded.size());
        } catch (ReflectiveOperationException e) {
            CustomApplesMod.LOGGER.error("Failed to register apple_bell on BlockEntityType.BELL", e);
        }
    }

    private static Unsafe getUnsafe() throws ReflectiveOperationException {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        return (Unsafe) theUnsafe.get(null);
    }
}
