package com.customapples.item;

import com.customapples.util.RedAppleFireHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class RedAppleFireBlock extends Block {
    public RedAppleFireBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.FIRE)
                .noCollission()
                .noOcclusion()
                .lightLevel(s -> 15)
                .strength(0f)
                .replaceable());
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 4);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        for (var entity : level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                new net.minecraft.world.phys.AABB(pos).inflate(0.5))) {
            RedAppleFireHelper.igniteMob(entity);
            RedAppleFireHelper.onMobInFire(entity);
        }
        level.scheduleTick(pos, this, 2 + random.nextInt(2));
    }
}
