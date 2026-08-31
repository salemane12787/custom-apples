package com.customapples.util;

import com.customapples.CustomApplesMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

public final class StructurePlacer {
    public static final ResourceLocation BIG_TREE =
            ResourceLocation.fromNamespaceAndPath(CustomApplesMod.MOD_ID, "big_tree");

    /** Big Tree schematic is ~81 x 114 x 81 — keep the center well away from the player. */
    private static final int HALF_WIDTH = 40;
    private static final int MIN_CENTER_DISTANCE = HALF_WIDTH + 64;
    private static final int MAX_CENTER_DISTANCE = MIN_CENTER_DISTANCE + 48;
    private static final int SPAWN_ATTEMPTS = 12;

    private StructurePlacer() {
    }

    public record BigTreePlacement(BlockPos treeCenter, BlockPos chestPos, Direction chestFacing) {
    }

    public static Optional<BigTreePlacement> placeBigTreeFarFrom(ServerLevel level, BlockPos playerPos) {
        StructureTemplate template = level.getStructureManager().getOrCreate(BIG_TREE);
        if (template.getSize().equals(Vec3i.ZERO)) {
            CustomApplesMod.LOGGER.error("Failed to load structure {}", BIG_TREE);
            return Optional.empty();
        }

        Vec3i size = template.getSize();
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(Rotation.NONE)
                .setIgnoreEntities(false);

        for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
            float angle = level.random.nextFloat() * Mth.TWO_PI;
            int distance = MIN_CENTER_DISTANCE + level.random.nextInt(MAX_CENTER_DISTANCE - MIN_CENTER_DISTANCE + 1);
            int centerX = playerPos.getX() + Mth.floor(Mth.cos(angle) * distance);
            int centerZ = playerPos.getZ() + Mth.floor(Mth.sin(angle) * distance);

            BlockPos ground = findSolidGround(level, centerX, centerZ);
            if (ground == null) {
                continue;
            }

            BlockPos origin = new BlockPos(
                    ground.getX() - size.getX() / 2,
                    ground.getY(),
                    ground.getZ() - size.getZ() / 2);

            template.placeInWorld(level, origin, origin, settings, level.getRandom(), 2);

            Optional<BigTreePlacement> placement = findSideChestPlacement(level, ground, playerPos);
            if (placement.isPresent()) {
                return placement;
            }
        }

        CustomApplesMod.LOGGER.warn("Could not find valid ground for Big Tree near {}", playerPos);
        return Optional.empty();
    }

    /**
     * Place the reward chest on the tree exterior facing the player so it is visible
     * on the trunk/leaves instead of buried in the center.
     */
    private static Optional<BigTreePlacement> findSideChestPlacement(
            ServerLevel level, BlockPos treeCenter, BlockPos playerPos) {
        double dx = playerPos.getX() - treeCenter.getX();
        double dz = playerPos.getZ() - treeCenter.getZ();
        double len = Math.hypot(dx, dz);
        if (len < 1.0) {
            dx = 0.0;
            dz = 1.0;
        } else {
            dx /= len;
            dz /= len;
        }
        Direction chestFacing = Direction.getNearest((float) dx, 0.0F, (float) dz);

        for (int yOff = 1; yOff <= 6; yOff++) {
            int y = treeCenter.getY() + yOff;
            for (int dist = HALF_WIDTH + 2; dist >= 4; dist--) {
                int surfaceX = treeCenter.getX() + Mth.floor(dx * dist);
                int surfaceZ = treeCenter.getZ() + Mth.floor(dz * dist);
                BlockPos surface = new BlockPos(surfaceX, y, surfaceZ);
                BlockPos chestPos = new BlockPos(
                        treeCenter.getX() + Mth.floor(dx * (dist + 1)),
                        y,
                        treeCenter.getZ() + Mth.floor(dz * (dist + 1)));

                if (isSolidTreeBlock(level, surface)
                        && level.isEmptyBlock(chestPos)
                        && level.isEmptyBlock(chestPos.above())) {
                    return Optional.of(new BigTreePlacement(treeCenter, chestPos, chestFacing));
                }
            }
        }

        // Fallback: chest on the ground just outside the tree footprint.
        BlockPos fallback = new BlockPos(
                treeCenter.getX() + Mth.floor(dx * (HALF_WIDTH + 3)),
                treeCenter.getY(),
                treeCenter.getZ() + Mth.floor(dz * (HALF_WIDTH + 3)));
        if (level.isEmptyBlock(fallback) && isSolidTreeBlock(level, fallback.below())) {
            return Optional.of(new BigTreePlacement(treeCenter, fallback, chestFacing));
        }

        return Optional.empty();
    }

    private static boolean isSolidTreeBlock(ServerLevel level, BlockPos pos) {
        return !level.isEmptyBlock(pos) && level.getFluidState(pos).isEmpty();
    }

    private static BlockPos findSolidGround(ServerLevel level, int x, int z) {
        int minY = level.getMinBuildHeight();
        for (int y = level.getMaxBuildHeight() - 1; y >= minY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.isEmptyBlock(pos) && level.getFluidState(pos).isEmpty()) {
                return pos.above();
            }
        }
        return null;
    }
}
