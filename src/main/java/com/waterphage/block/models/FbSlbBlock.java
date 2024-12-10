package com.waterphage.block.models;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import static com.waterphage.Fbased.fbKeyFlip;

public class FbSlbBlock extends FacingBlock implements Waterloggable {
    public static final BooleanProperty SINGLE = BooleanProperty.of("single_slab");

    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(SINGLE);
        stateManager.add(Properties.FACING);
        stateManager.add(Properties.WATERLOGGED);
    }

    public SoundEvent blockPlaceSound;

    public FbSlbBlock(Settings settings, SoundEvent blockSoundEvent) {
        super(settings);

        setDefaultState(this.stateManager.getDefaultState().with(SINGLE, true));
        setDefaultState(this.stateManager.getDefaultState().with(Properties.FACING, Direction.UP));
        this.setDefaultState((BlockState) ((BlockState) this.getDefaultState().with(Properties.WATERLOGGED, false)));

        blockPlaceSound = blockSoundEvent;
    }

    public void playPlaceSound(World world, BlockPos pos) {
        world.playSound(
                null, pos, blockPlaceSound, SoundCategory.BLOCKS, 1f, 0.75f);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx) {

        // changes hitbox depending on block state

        Direction dir = state.get(FACING);
        if (!state.get(SINGLE)) {
            return VoxelShapes.fullCube();
        } else switch (dir){
                case DOWN -> {return VoxelShapes.cuboid(0.0f, 0.0f, 0.0f, 1.0f, 0.5f, 1.0f);}
                case UP -> {return VoxelShapes.cuboid(0.0f, 0.5f, 0.0f, 1.0f, 1.0f, 1.0f);}
                case NORTH -> {return VoxelShapes.cuboid(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.5f);}
                case SOUTH -> {return VoxelShapes.cuboid(0.0f, 0.0f, 0.5f, 1.0f, 1.0f, 1.0f);}
                case EAST -> {return VoxelShapes.cuboid(0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);}
                default -> {return VoxelShapes.cuboid(0.0f, 0.0f, 0.0f, 0.5f, 1.0f, 1.0f);}
            }
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

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand,
                              BlockHitResult hit) {

        ActionResult ACTION;

        ACTION = combineSlab(state, world, pos, player, hand, hit);

        return ACTION;
    }

    public ActionResult combineSlab(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
                                     BlockHitResult hit) {

        player.getStackInHand(hand);
        // checks if player clicked on block with same item type
        if (player.getAbilities().allowModifyWorld
                && ItemStack.areItemsEqual(new ItemStack(this), player.getStackInHand(hand))
                && state.get(SINGLE)) {

            ActionResult RESULT = ActionResult.FAIL;
            player.getBlockX();

            if (state.get(SINGLE)) {
                Direction HITSIDE = hit.getSide();
                // is facing north and hit from south
                if (state.get(FACING) == Direction.NORTH) {
                    if (HITSIDE == Direction.SOUTH) {
                        world.setBlockState(pos, state.with(SINGLE, false));RESULT = sucessfulPlace(player, hand, world, pos);
                    }
                }
                // is facing east and hit from west
                else if (state.get(FACING) == Direction.EAST) {
                    if (HITSIDE == Direction.WEST) {
                        world.setBlockState(pos, state.with(SINGLE, false));RESULT = sucessfulPlace(player, hand, world, pos);
                    }
                }
                // is facing south and hit from north
                else if (state.get(FACING) == Direction.SOUTH) {
                    if (HITSIDE == Direction.NORTH) {
                        world.setBlockState(pos, state.with(SINGLE, false));RESULT = sucessfulPlace(player, hand, world, pos);
                    }
                }
                // is facing west and hit from east
                else if (state.get(FACING) == Direction.WEST) {
                    if (HITSIDE == Direction.EAST) {
                        world.setBlockState(pos, state.with(SINGLE, false));RESULT = sucessfulPlace(player, hand, world, pos);
                    }
                }
                // is facing west and hit from east
                else if (state.get(FACING) == Direction.UP) {
                    if (HITSIDE == Direction.DOWN) {
                        world.setBlockState(pos, state.with(SINGLE, false));RESULT = sucessfulPlace(player, hand, world, pos);
                    }
                }
                // is facing west and hit from east
                else if (state.get(FACING) == Direction.DOWN) {
                    if (HITSIDE == Direction.UP) {
                        world.setBlockState(pos, state.with(SINGLE, false));RESULT = sucessfulPlace(player, hand, world, pos);
                    }
                }

                else {

                }

            }

            return RESULT;
        } else {
            return ActionResult.PASS;
        }

    }

    public ActionResult sucessfulPlace(PlayerEntity player, Hand hand, World world, BlockPos pos) {
        // checks if player is in creative and removes 1 item if not
        boolean isInCreative = player.getAbilities().creativeMode;
        if (!isInCreative) {
            player.getStackInHand(hand).setCount(player.getStackInHand(hand).getCount() - 1);
        }
        playPlaceSound(world, pos);

        // makes arm swing
        return ActionResult.success(world.isClient);
    }
    @Environment(EnvType.CLIENT)
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        if (fbKeyFlip==0||fbKeyFlip==4){
            return this.getDefaultState().with(Properties.FACING, ctx.getSide().getOpposite());
        }else if (fbKeyFlip==1||fbKeyFlip==5){
            return this.getDefaultState().with(Properties.FACING, ctx.getPlayerLookDirection().getOpposite());
        }else if (fbKeyFlip==2||fbKeyFlip==6){
            return this.getDefaultState().with(Properties.FACING, ctx.getSide());
        }else {
            return this.getDefaultState().with(Properties.FACING, ctx.getPlayerLookDirection());
        }
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (state.get(SINGLE)!=false) {
            return Waterloggable.super.tryFillWithFluid(world, pos, state, fluidState);
        }
        return false;
    }

    public boolean canFillWithFluid(@Nullable PlayerEntity player, BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        if (state.get(SINGLE)!=false) {
            return Waterloggable.super.canFillWithFluid(player, world, pos, state, fluid);
        }
        return false;
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