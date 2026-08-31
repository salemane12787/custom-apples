package com.customapples.util;

import com.customapples.entity.ModEntities;
import com.customapples.entity.WormEntity;
import com.customapples.network.ModNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class WormNaming {
    private WormNaming() {
    }

    public static WormEntity spawnAndPromptName(ServerPlayer player, Level level, double x, double y, double z) {
        WormEntity worm = ModEntities.WORM.get().create(level);
        if (worm == null) {
            return null;
        }
        worm.moveTo(x, y, z, player.getYRot(), 0);
        level.addFreshEntity(worm);
        ModNetworking.sendOpenWormName(player, worm.getId());
        return worm;
    }
}
