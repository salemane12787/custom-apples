package com.customapples.fluid;

import com.customapples.CustomApplesMod;
import com.customapples.fluid.AppleJuicePourTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, CustomApplesMod.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, CustomApplesMod.MOD_ID);

    public static final RegistryObject<FluidType> APPLE_JUICE_TYPE = FLUID_TYPES.register("apple_juice",
            () -> new FluidType(FluidType.Properties.create()
                    .canSwim(true)
                    .canDrown(true)
                    .canConvertToSource(false)
                    .supportsBoating(true)
                    .fallDistanceModifier(0.5F)
                    .motionScale(0.014D)
                    .pathType(BlockPathTypes.WATER)
                    .adjacentPathType(BlockPathTypes.WATER_BORDER)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY))
            {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(com.customapples.client.ModFluidClient.APPLE_JUICE_EXTENSIONS);
                }
            });

    public static final RegistryObject<FlowingFluid> FLOWING_APPLE_JUICE = FLUIDS.register("flowing_apple_juice",
            AppleJuiceFlowing::new);
    public static final RegistryObject<FlowingFluid> APPLE_JUICE = FLUIDS.register("apple_juice",
            AppleJuiceSource::new);

    public static void register(IEventBus bus) {
        FLUID_TYPES.register(bus);
        FLUIDS.register(bus);
    }

    private static ForgeFlowingFluid.Properties appleJuiceProperties() {
        return new ForgeFlowingFluid.Properties(APPLE_JUICE_TYPE, APPLE_JUICE, FLOWING_APPLE_JUICE)
                .block(com.customapples.item.ModBlocks.APPLE_JUICE)
                .bucket(com.customapples.item.ModItems.APPLE_BUCKET_JUICE)
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .tickRate(5);
    }

    public static BlockBehaviour.Properties appleJuiceBlockProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_ORANGE)
                .replaceable()
                .pushReaction(PushReaction.DESTROY)
                .noLootTable()
                .liquid()
                .strength(100.0F)
                .noCollission()
                .noOcclusion()
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false);
    }

    public static class AppleJuiceLiquidBlock extends LiquidBlock {
        public AppleJuiceLiquidBlock(Supplier<? extends FlowingFluid> fluid, Properties properties) {
            super(fluid, properties);
        }

        @Override
        public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
            super.onPlace(state, level, pos, oldState, isMoving);
            if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
                return;
            }
            FluidState newFluid = state.getFluidState();
            FluidState oldFluid = oldState.getFluidState();
            if (!isAppleJuice(newFluid) || isAppleJuice(oldFluid)) {
                return;
            }
            AppleJuicePourTracker.scheduleTreeGrowth(serverLevel, pos);
        }

        @Override
        public void entityInside(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.Entity entity) {
            entity.resetFallDistance();
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.9, 0.6, 0.9));
        }
    }

    public static boolean isAppleJuice(FluidState state) {
        Fluid type = state.getType();
        return type.isSame(APPLE_JUICE.get()) || type.isSame(FLOWING_APPLE_JUICE.get());
    }

    private static final class AppleJuiceSource extends ForgeFlowingFluid.Source {
        private AppleJuiceSource() {
            super(appleJuiceProperties());
        }

        @Override
        protected void spreadTo(LevelAccessor level, BlockPos pos, BlockState blockState,
                                  Direction direction, FluidState fluidState) {
            AppleJuiceSpreadLogic.spread(this, super::spreadTo, level, pos, blockState, direction, fluidState);
        }
    }

    private static final class AppleJuiceFlowing extends ForgeFlowingFluid.Flowing {
        private AppleJuiceFlowing() {
            super(appleJuiceProperties());
        }

        @Override
        protected void spreadTo(LevelAccessor level, BlockPos pos, BlockState blockState,
                                  Direction direction, FluidState fluidState) {
            AppleJuiceSpreadLogic.spread(this, super::spreadTo, level, pos, blockState, direction, fluidState);
        }
    }

    private static final class AppleJuiceSpreadLogic {
        @FunctionalInterface
        private interface SpreadAction {
            void spread(LevelAccessor level, BlockPos pos, BlockState blockState,
                          Direction direction, FluidState fluidState);
        }

        private AppleJuiceSpreadLogic() {
        }

        private static void spread(Fluid fluid, SpreadAction spreadAction, LevelAccessor level, BlockPos pos,
                                   BlockState blockState, Direction direction, FluidState fluidState) {
            BlockPos targetPos = pos.relative(direction);
            BlockState targetState = level.getBlockState(targetPos);

            if (targetState.getBlock() instanceof LeavesBlock) {
                if (level instanceof ServerLevel serverLevel) {
                    level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), 3);
                    serverLevel.sendParticles(ParticleTypes.FALLING_NECTAR,
                            targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                            3, 0.3, 0.3, 0.3, 0);
                    if (serverLevel.getRandom().nextFloat() < 0.35f) {
                        Block.popResource(serverLevel, targetPos,
                                new ItemStack(Items.APPLE, 1 + serverLevel.getRandom().nextInt(2)));
                    }
                }
                return;
            }

            spreadAction.spread(level, pos, blockState, direction, fluidState);
        }
    }

    public static class AppleWaterBlock extends Block {
        public AppleWaterBlock() {
            super(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WATER)
                    .strength(100f)
                    .noCollission()
                    .noOcclusion()
                    .replaceable());
        }

        @Override
        public void entityInside(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.Entity entity) {
            entity.resetFallDistance();
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.9, 0.8, 0.9));
            if (!entity.isOnFire()) {
                entity.clearFire();
            }
        }
    }
}
