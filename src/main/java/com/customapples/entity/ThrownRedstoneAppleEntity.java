package com.customapples.entity;

import com.customapples.item.ModItems;
import com.customapples.util.TerrainDestroyer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class ThrownRedstoneAppleEntity extends ThrowableItemProjectile {
    public ThrownRedstoneAppleEntity(EntityType<? extends ThrownRedstoneAppleEntity> type, Level level) {
        super(type, level);
    }

    public ThrownRedstoneAppleEntity(Level level, LivingEntity thrower) {
        super(ModEntities.THROWN_REDSTONE_APPLE.get(), thrower, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.REDSTONE_APPLE.get();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide) {
            return;
        }
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    getX(), getY(), getZ(),
                    40, 1.5, 1.5, 1.5, 0.1);
            serverLevel.sendParticles(
                    ParticleTypes.FLASH,
                    getX(), getY(), getZ(),
                    1, 0, 0, 0, 0);
        }
        TerrainDestroyer.blastCylinderDown(level(), blockPosition());
        discard();
    }
}
