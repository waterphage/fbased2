package com.waterphage.block.models;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import com.waterphage.block.ModMaterials;
import net.minecraft.block.*;
import net.minecraft.block.enums.Thickness;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.GameEvent.Emitter;
import org.jetbrains.annotations.Nullable;

import static com.waterphage.block.ModTags.FBSPIKE;

public class FbSpike  extends Block implements LandingBlock, Waterloggable {
    public static final DirectionProperty DIRECTION;
    public static final IntProperty THICKNESS;
    public static final BooleanProperty WATERLOGGED;
    public FbSpike(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState) ((BlockState) ((BlockState) ((BlockState) this.stateManager.getDefaultState()).with(DIRECTION, Direction.UP)).with(THICKNESS, 0)).with(WATERLOGGED, false));
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{DIRECTION, THICKNESS, WATERLOGGED});
    }
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction direction = ctx.getSide();
        BlockPos npos=ctx.getBlockPos().add(direction.getVector());
        World w=ctx.getWorld();
        if (w.getBlockState(npos).getBlock().equals(this)){
            return this.getDefaultState()
                    .with(DIRECTION, direction)
                    .with(THICKNESS, w.getBlockState(npos).get(THICKNESS))
                    .with(WATERLOGGED,
                            ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
        }

        return this.getDefaultState()
                .with(DIRECTION, direction)
                .with(THICKNESS, 0)
                .with(WATERLOGGED,
                        ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
    }

    static {
        DIRECTION = Properties.FACING;
        THICKNESS = IntProperty.of("thickness", 0, 3);;
        WATERLOGGED = Properties.WATERLOGGED;
        VoxelShape[] shapes={
                Block.createCuboidShape(4.9, 0.0, 4.9, 11.1, 16.0, 11.1),
                Block.createCuboidShape(2.6, 0.0, 2.6, 13.4, 16.0, 13.4),
                Block.createCuboidShape(1.3, 0.0, 1.3, 14.6, 16.0, 14.6),
                Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
        };
    }
    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity && entity.getType() != EntityType.FOX && entity.getType() != EntityType.BEE) {
            entity.slowMovement(state, new Vec3d(0.800000011920929, 0.75, 0.800000011920929));
            if (!world.isClient && (entity.lastRenderX != entity.getX() || entity.lastRenderZ != entity.getZ())) {
                double d = Math.abs(entity.getX() - entity.lastRenderX);
                double e = Math.abs(entity.getZ() - entity.lastRenderZ);
                if (d >= 0.003000000026077032 || e >= 0.003000000026077032) {
                    entity.damage(world.getDamageSources().sweetBerryBush(), 1.0F);
                }
            }

        }
    }
    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }
    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        RegistryEntry<Biome> b= world.getBiome(pos);
        ModMaterials.climatizer(state,world,pos,b);
        this.scheduledTick(state, world, pos, random);
    }
    @Override
    public BlockState getStateForNeighborUpdate(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            WorldAccess world,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(
                    pos, Fluids.WATER, Fluids.WATER.getTickRate(world)
            );
        }
        Direction dir=state.get(DIRECTION);
        if (direction.equals(dir)) {
            if (!neighborState.isOf(this)){return state;}
            int nt = neighborState.get(THICKNESS);
            int no = state.get(THICKNESS);
            if(dir.equals(neighborState.get(DIRECTION))){
                if(nt>no-1){return state.with(THICKNESS, Math.min(nt + 1, 3));}
            }else{
                if(nt>no){return state.with(THICKNESS,nt);}
            }
        }
        if (!canPlaceAt(state, world, pos)) {
            world.scheduleBlockTick(pos, this, 1);
        }

        return state;
    }
    @Override
    public void scheduledTick(
            BlockState state,
            ServerWorld world,
            BlockPos pos,
            Random random
    ) {
        if (!canPlaceAt(state, world, pos)) {
            FallingBlockEntity.spawnFromBlock(world, pos, state);
        }
    }
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return canPlaceAtWithDirection(world, pos, (Direction) state.get(DIRECTION));
    }
    private static boolean canPlaceAtWithDirection(WorldView world, BlockPos pos, Direction direction) {
        BlockPos blockPos = pos.offset(direction.getOpposite());
        BlockState blockState = world.getBlockState(blockPos);
        return blockState.isSideSolidFullSquare(world, blockPos, direction) || isPointedDripstoneFacingDirection(blockState, direction);
    }
    private static boolean isPointedDripstoneFacingDirection(
            BlockState state, Direction direction) {
        return state.getBlock() instanceof FbSpike
                && state.get(DIRECTION) == direction;
    }
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (state.get(DIRECTION) == Direction.UP && state.get(THICKNESS) == 0) {
            entity.handleFallDamage(fallDistance + 2.0F, 2.0F, world.getDamageSources().stalagmite());
        } else {
            super.onLandedUpon(world, state, pos, entity, fallDistance);
        }

    }
    public void onDestroyedOnLanding(World world, BlockPos pos, FallingBlockEntity fallingBlockEntity) {
        if (!fallingBlockEntity.isSilent()) {
            world.syncWorldEvent(1045, pos, 0);
        }
    }
    public DamageSource getDamageSource(Entity attacker) {
        return attacker.getDamageSources().fallingStalactite(attacker);
    }
    @Override
    public boolean isSideInvisible(BlockState state, BlockState adjacent, Direction side) {
        return false;
    }
}
