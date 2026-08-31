package com.customapples.entity;

import com.customapples.item.ModItems;
import com.customapples.util.AppleTreeHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class ThrownAppleBombEntity extends ThrowableItemProjectile {
    public ThrownAppleBombEntity(EntityType<? extends ThrownAppleBombEntity> type, Level level) {
        super(type, level);
    }

    public ThrownAppleBombEntity(Level level, LivingEntity thrower) {
        super(ModEntities.THROWN_APPLE_BOMB.get(), thrower, level);
    }

    public ThrownAppleBombEntity(Level level) {
        super(ModEntities.THROWN_APPLE_BOMB.get(), level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.APPLE_APPLE_APPLE.get();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide) {
            return;
        }
        level().explode(this, getX(), getY(), getZ(), 2.0f, false, Level.ExplosionInteraction.NONE);
        AppleTreeHelper.spawnGiantAppleTrees(level(), blockPosition(), 4);

        if (!getPersistentData().getBoolean("CustomApplesNoSplit")) {
            spawnChildren();
        }
        discard();
    }

    private void spawnChildren() {
        for (Direction dir : Direction.values()) {
            ThrownAppleBombEntity child = new ThrownAppleBombEntity(level());
            if (getOwner() != null) {
                child.setOwner(getOwner());
            }
            child.getPersistentData().putBoolean("CustomApplesNoSplit", true);
            child.setPos(getX(), getY(), getZ());
            double spread = 0.35;
            child.shoot(
                    dir.getStepX() + (random.nextDouble() - 0.5) * spread,
                    dir.getStepY() + 0.2 + random.nextDouble() * 0.3,
                    dir.getStepZ() + (random.nextDouble() - 0.5) * spread,
                    0.65f,
                    2.0f);
            level().addFreshEntity(child);
        }
    }
}
