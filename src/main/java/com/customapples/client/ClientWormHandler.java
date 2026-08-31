package com.customapples.client;

import com.customapples.CustomApplesMod;
import com.customapples.entity.WormEntity;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CustomApplesMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientWormHandler {
    private ClientWormHandler() {
    }

    public static void openNameScreen(int entityId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        var entity = mc.level.getEntity(entityId);
        if (entity instanceof WormEntity worm) {
            mc.setScreen(new WormNameScreen(worm));
            return;
        }
        mc.execute(() -> {
            if (mc.level != null) {
                var retry = mc.level.getEntity(entityId);
                if (retry instanceof WormEntity worm) {
                    mc.setScreen(new WormNameScreen(worm));
                }
            }
        });
    }
}
