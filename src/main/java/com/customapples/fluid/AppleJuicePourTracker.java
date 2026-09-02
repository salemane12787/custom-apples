package com.customapples.fluid;

import com.customapples.util.AppleTreeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;

/** Each juice block grows a tree 3 seconds after that block receives juice. */
public final class AppleJuicePourTracker {
  /** 3 seconds at 20 ticks per second. */
  private static final int TREE_DELAY_TICKS = 60;

  private AppleJuicePourTracker() {}

  public static void scheduleTreeGrowth(ServerLevel level, BlockPos juicePos) {
    int plantTick = level.getServer().getTickCount() + TREE_DELAY_TICKS;
    level.getServer().tell(new TickTask(plantTick, () -> {
      if (ModFluids.isAppleJuice(level.getFluidState(juicePos))) {
        AppleTreeHelper.growAppleTreeAboveJuice(level, juicePos);
      }
    }));
  }
}
