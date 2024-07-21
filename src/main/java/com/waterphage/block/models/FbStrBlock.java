package com.waterphage.block.models;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

import static com.waterphage.Fbased.fbKeyFlip;

public class FbStrBlock extends FacingBlock implements Waterloggable {

    static final VoxelShape U_N_E_SHP;
    static final VoxelShape U_N_W_SHP;
    static final VoxelShape U_S_E_SHP;
    static final VoxelShape U_S_W_SHP;
    static final VoxelShape D_N_E_SHP;
    static final VoxelShape D_N_W_SHP;
    static final VoxelShape D_S_E_SHP;
    static final VoxelShape D_S_W_SHP;

    static{

        U_N_E_SHP = Block.createCuboidShape(8.0, 8.0, 0.0, 16.0, 16.0, 8.0);
        U_N_W_SHP = Block.createCuboidShape(0.0, 8.0, 0.0, 8.0, 16.0, 8.0);
        U_S_E_SHP = Block.createCuboidShape(8.0, 8.0, 8.0, 16.0, 16.0, 16.0);
        U_S_W_SHP = Block.createCuboidShape(0.0, 8.0, 8.0, 8.0, 16.0, 16.0);
        D_N_E_SHP = Block.createCuboidShape(8.0, 0.0, 0.0, 16.0, 8.0, 8.0);
        D_N_W_SHP = Block.createCuboidShape(0.0, 0.0, 0.0, 8.0, 8.0, 8.0);
        D_S_E_SHP = Block.createCuboidShape(8.0, 0.0, 8.0, 16.0, 8.0, 16.0);
        D_S_W_SHP = Block.createCuboidShape(0.0, 0.0, 8.0, 8.0, 8.0, 16.0);
    }
    public static final DirectionProperty NEW_DIR = DirectionProperty.of("rotation",Direction.Type.HORIZONTAL);

    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(NEW_DIR);
        stateManager.add(Properties.FACING);
        stateManager.add(Properties.WATERLOGGED);

    }
    public FbStrBlock(Settings settings) {
        super(settings);

        setDefaultState(this.stateManager.getDefaultState().with(NEW_DIR, Direction.NORTH));
        setDefaultState(this.stateManager.getDefaultState().with(Properties.FACING, Direction.UP));
        this.setDefaultState((BlockState) ((BlockState) this.getDefaultState().with(Properties.WATERLOGGED, false)));

    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx) {

        // changes hitbox depending on block state

        Direction dir = state.get(FACING);
        Direction rot = state.get(NEW_DIR);

        if (dir==Direction.UP){
            if (rot==Direction.SOUTH) {
                return VoxelShapes.union(D_N_E_SHP,D_N_W_SHP,D_S_W_SHP,D_S_E_SHP, U_S_W_SHP,U_S_E_SHP);
            }else if (rot==Direction.WEST) {
                return VoxelShapes.union(D_N_E_SHP,D_N_W_SHP,D_S_W_SHP,D_S_E_SHP, U_S_W_SHP,U_N_W_SHP);
            }else if (rot==Direction.NORTH) {
                return VoxelShapes.union(D_N_E_SHP,D_N_W_SHP,D_S_W_SHP,D_S_E_SHP, U_N_W_SHP,U_N_E_SHP);
            }else {
                return VoxelShapes.union(D_N_E_SHP,D_N_W_SHP,D_S_W_SHP,D_S_E_SHP, U_S_E_SHP,U_N_E_SHP);
            }
        }

        if (dir==Direction.DOWN){
            if (rot==Direction.SOUTH) {
                return VoxelShapes.union(U_N_E_SHP,U_N_W_SHP,U_S_W_SHP,U_S_E_SHP, D_S_W_SHP,D_S_E_SHP);
            }else if (rot==Direction.WEST) {
                return VoxelShapes.union(U_N_E_SHP,U_N_W_SHP,U_S_W_SHP,U_S_E_SHP, D_S_W_SHP,D_N_W_SHP);
            }else if (rot==Direction.NORTH) {
                return VoxelShapes.union(U_N_E_SHP,U_N_W_SHP,U_S_W_SHP,U_S_E_SHP, D_N_W_SHP,D_N_E_SHP);
            }else {
                return VoxelShapes.union(U_N_E_SHP,U_N_W_SHP,U_S_W_SHP,U_S_E_SHP, D_S_E_SHP,D_N_E_SHP);
            }
        }

        if (dir==Direction.WEST){
            if (rot==Direction.SOUTH) {
                return VoxelShapes.union(U_N_E_SHP,D_N_E_SHP,D_S_E_SHP,U_S_E_SHP, D_S_W_SHP,D_N_W_SHP);
            }else if (rot==Direction.WEST) {
                return VoxelShapes.union(U_N_E_SHP,D_N_E_SHP,D_S_E_SHP,U_S_E_SHP, U_N_W_SHP,D_N_W_SHP);
            }else if (rot==Direction.NORTH) {
                return VoxelShapes.union(U_N_E_SHP,D_N_E_SHP,D_S_E_SHP,U_S_E_SHP, U_N_W_SHP,U_S_W_SHP);
            }else {
                return VoxelShapes.union(U_N_E_SHP,D_N_E_SHP,D_S_E_SHP,U_S_E_SHP, U_S_W_SHP,D_S_W_SHP);
            }
        }

        if (dir==Direction.EAST){
            if (rot==Direction.SOUTH) {
                return VoxelShapes.union(U_N_W_SHP,D_N_W_SHP,D_S_W_SHP,U_S_W_SHP, D_S_E_SHP,D_N_E_SHP);
            }else if (rot==Direction.EAST) {
                return VoxelShapes.union(U_N_W_SHP,D_N_W_SHP,D_S_W_SHP,U_S_W_SHP, U_S_E_SHP,D_S_E_SHP);
            }else if (rot==Direction.NORTH) {
                return VoxelShapes.union(U_N_W_SHP,D_N_W_SHP,D_S_W_SHP,U_S_W_SHP, U_N_E_SHP,U_S_E_SHP);
            }else {
                return VoxelShapes.union(U_N_W_SHP,D_N_W_SHP,D_S_W_SHP,U_S_W_SHP, U_N_E_SHP,D_N_E_SHP);
            }
        }

        if (dir==Direction.SOUTH){
            if (rot==Direction.EAST) {
                return VoxelShapes.union(U_N_W_SHP,D_N_W_SHP,D_N_E_SHP,U_N_E_SHP, D_S_W_SHP,D_S_E_SHP);
            }else if (rot==Direction.SOUTH) {
                return VoxelShapes.union(U_N_W_SHP,D_N_W_SHP,D_N_E_SHP,U_N_E_SHP, U_S_W_SHP,D_S_W_SHP);
            }else if (rot==Direction.WEST) {
                return VoxelShapes.union(U_N_W_SHP,D_N_W_SHP,D_N_E_SHP,U_N_E_SHP, U_S_E_SHP,U_S_W_SHP);
            }else {
                return VoxelShapes.union(U_N_W_SHP,D_N_W_SHP,D_N_E_SHP,U_N_E_SHP, U_S_E_SHP,D_S_E_SHP);
            }
        }

        if (dir==Direction.NORTH){
            if (rot==Direction.EAST) {
                return VoxelShapes.union(U_S_W_SHP,D_S_W_SHP,D_S_E_SHP,U_S_E_SHP, D_N_E_SHP,D_N_W_SHP);
            }else if (rot==Direction.NORTH) {
                return VoxelShapes.union(U_S_W_SHP,D_S_W_SHP,D_S_E_SHP,U_S_E_SHP, U_N_E_SHP,D_N_E_SHP);
            }else if (rot==Direction.WEST) {
                return VoxelShapes.union(U_S_W_SHP,D_S_W_SHP,D_S_E_SHP,U_S_E_SHP, U_N_W_SHP,U_N_E_SHP);
            }else {
                return VoxelShapes.union(U_S_W_SHP,D_S_W_SHP,D_S_E_SHP,U_S_E_SHP, U_N_W_SHP,D_N_W_SHP);
            }
        }


        return VoxelShapes.fullCube();

    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return true;
    }
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(Properties.WATERLOGGED).booleanValue()) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    public BlockState correct(Direction dir1,Direction dir2) {
    if(dir2==Direction.UP){
        if (dir1==Direction.UP||dir1==Direction.DOWN){
                return this.getDefaultState().with(Properties.FACING, dir1).with(NEW_DIR, Direction.NORTH);
            }
            return this.getDefaultState().with(Properties.FACING, dir1).with(NEW_DIR, dir1);
        }
        if(dir2==Direction.DOWN){
            if (dir1==Direction.UP||dir1==Direction.DOWN){
                return this.getDefaultState().with(Properties.FACING, dir1).with(NEW_DIR, Direction.SOUTH);
            }
            return this.getDefaultState().with(Properties.FACING, dir1).with(NEW_DIR, dir1);
        }
        return this.getDefaultState().with(Properties.FACING, dir1).with(NEW_DIR, dir2);
    }
    @Environment(EnvType.CLIENT)
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {

        if (fbKeyFlip==0){
            return correct(ctx.getSide(),ctx.getPlayerLookDirection());
        }else if (fbKeyFlip==1){
            return correct(ctx.getPlayerLookDirection(),ctx.getSide());
        }else if (fbKeyFlip==2){
            return correct(ctx.getSide().getOpposite(),ctx.getPlayerLookDirection());
        }else if (fbKeyFlip==3){
            return correct(ctx.getPlayerLookDirection().getOpposite(),ctx.getSide());
        }else if (fbKeyFlip==4){
            return correct(ctx.getSide(),ctx.getPlayerLookDirection().getOpposite());
        }else if (fbKeyFlip==5){
            return correct(ctx.getPlayerLookDirection(),ctx.getSide().getOpposite());
        }else if (fbKeyFlip==6){
            return correct(ctx.getSide().getOpposite(),ctx.getPlayerLookDirection().getOpposite());
        }else {
            return correct(ctx.getPlayerLookDirection().getOpposite(),ctx.getSide().getOpposite());
        }
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        if (state.get(Properties.WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        // return state.getFluidState();
        return state.getFluidState();
    }



}
