package com.customapples.item;

import com.customapples.block.ModBlockEntities;
import com.customapples.util.TerracottaModeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class AppleBellBlock extends BellBlock {
    public AppleBellBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BellBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(
                type,
                ModBlockEntities.APPLE_BELL.get(),
                level.isClientSide ? BellBlockEntity::clientTick : BellBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        InteractionResult result = super.use(state, level, pos, player, hand, hit);
        if (!level.isClientSide && result.consumesAction() && level.getServer() != null) {
            level.getServer().execute(() -> TerracottaModeHelper.ringBell(level, pos, 16));
        }
        return result;
    }
}
