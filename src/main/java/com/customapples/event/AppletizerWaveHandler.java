package com.customapples.event;

import com.customapples.util.BlockBreakGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = com.customapples.CustomApplesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AppletizerWaveHandler {
    private static final List<AppletizerWave> WAVES = new ArrayList<>();

    private AppletizerWaveHandler() {
    }

    public static void start(ServerLevel level, BlockPos origin, Direction facing) {
        WAVES.add(new AppletizerWave(level, origin, facing));
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide()) {
            return;
        }
        if (!(event.level instanceof ServerLevel serverLevel)) {
            return;
        }
        Iterator<AppletizerWave> it = WAVES.iterator();
        while (it.hasNext()) {
            AppletizerWave wave = it.next();
            if (wave.level != serverLevel) {
                continue;
            }
            if (wave.tick()) {
                it.remove();
            }
        }
    }

    private static final class AppletizerWave {
        private static final int START_OFFSET = 2;
        private static final int MAX_FORWARD = 80;
        private static final int MIN_HALF_WIDTH = 1;
        private static final int MAX_HALF_WIDTH = 18;

        private final ServerLevel level;
        private final int originX;
        private final int originZ;
        private final Direction facing;
        private final Direction right;
        private final int minY;
        private int forwardStep;
        private int delay;

        private AppletizerWave(ServerLevel level, BlockPos origin, Direction facing) {
            this.level = level;
            this.originX = origin.getX();
            this.originZ = origin.getZ();
            this.facing = facing;
            this.right = facing.getClockWise();
            this.minY = level.getMinBuildHeight();
            this.forwardStep = START_OFFSET;
            this.delay = 0;
        }

        private boolean tick() {
            if (delay > 0) {
                delay--;
                return false;
            }
            if (forwardStep > MAX_FORWARD) {
                return true;
            }

            float progress = (forwardStep - START_OFFSET) / (float) (MAX_FORWARD - START_OFFSET);
            int halfWidth = MIN_HALF_WIDTH + (int) (progress * (MAX_HALF_WIDTH - MIN_HALF_WIDTH));
            breakWaveSlice(forwardStep, halfWidth);

            forwardStep++;
            delay = 2;
            return false;
        }

        private void breakWaveSlice(int forward, int halfWidth) {
            int sliceX = originX + facing.getStepX() * forward;
            int sliceZ = originZ + facing.getStepZ() * forward;

            for (int side = -halfWidth; side <= halfWidth; side++) {
                int x = sliceX + right.getStepX() * side;
                int z = sliceZ + right.getStepZ() * side;
                if (isTooCloseToOrigin(x, z)) {
                    continue;
                }
                int topY = findColumnTop(x, z);
                for (int y = topY; y >= minY; y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!BlockBreakGuard.canBreak(level, pos)) {
                        continue;
                    }
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }

            int midY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, sliceX, sliceZ);
            level.playSound(null, new BlockPos(sliceX, midY, sliceZ),
                    SoundEvents.STONE_BREAK, SoundSource.BLOCKS,
                    0.4f + halfWidth * 0.02f, 0.45f + level.random.nextFloat() * 0.35f);
            level.sendParticles(ParticleTypes.EXPLOSION,
                    sliceX + 0.5, midY + 0.5, sliceZ + 0.5,
                    Math.min(10, halfWidth / 2 + 1), halfWidth * 0.2, 0.5, halfWidth * 0.2, 0.01);
            level.sendParticles(ParticleTypes.END_ROD,
                    sliceX + 0.5, midY + 0.5, sliceZ + 0.5,
                    Math.min(16, halfWidth), halfWidth * 0.25, 0.4, halfWidth * 0.25, 0.02);
        }

        private boolean isTooCloseToOrigin(int x, int z) {
            int dx = x - originX;
            int dz = z - originZ;
            return dx * dx + dz * dz < START_OFFSET * START_OFFSET;
        }

        private int findColumnTop(int x, int z) {
            int maxY = level.getMaxBuildHeight() - 1;
            for (int y = maxY; y >= minY; y--) {
                if (!level.getBlockState(new BlockPos(x, y, z)).isAir()) {
                    return y;
                }
            }
            return minY;
        }
    }
}
