package com.waterphage.block.models;

import com.waterphage.meta.Storage;
import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import static com.waterphage.meta.Storage.*;

public class FbMistBlock extends Block {
    public FbMistBlock(Settings settings) {
        super(settings);
    }
    /*@Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        BlockPos up=pos.up();
        FluidState f=world.getFluidState(up);
        if (f.isOf(Fluids.FLOWING_WATER)) {
            world.setBlockState(up, Blocks.AIR.getDefaultState());
            world.playSound(null,pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 0.5F);
        }

    }*/
    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }
    @Override
    public boolean isSideInvisible(BlockState state, BlockState adjacent, Direction side) {
        return false;
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state,
            BlockView world,
            BlockPos pos,
            ShapeContext context
    ) {
        return VoxelShapes.empty();
    }
    @Override
    public VoxelShape getOutlineShape(
            BlockState state,
            BlockView world,
            BlockPos pos,
            ShapeContext context
    ) {
        return VoxelShapes.empty();
    }

    @Override
    public boolean canPathfindThrough(
            BlockState state,
            BlockView world,
            BlockPos pos,
            NavigationType type
    ) {
        return true;
    }
    @Override
    public void onBlockAdded(
            BlockState state,
            World world,
            BlockPos pos,
            BlockState oldState,
            boolean notify
    ) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        int tick=world.getFluidState(pos.down()).isOf(FLOW)?3:6;
        world.scheduleBlockTick(pos, this, tick);
        world.playSound(null,pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 0.5F);
    }
    private boolean check(ServerWorld world,BlockPos npos){
        FluidState w=world.getFluidState(npos);
        return w.isOf(FLOW);
    }

    @Override
    public void scheduledTick(
            BlockState state,
            ServerWorld world,
            BlockPos pos,
            Random random
    ) {
        if (world.getFluidState(pos.down()).isOf(FLOW)){
            //if (world.getFluidState(pos.down(2)).isOf(FLOW)){
            //    world.setBlockState(pos.down(2), MIST);}
            world.setBlockState(pos.down(), MIST);
            world.scheduleBlockTick(pos, this, 6);return;}
        world.removeBlock(pos,false);
    }
}