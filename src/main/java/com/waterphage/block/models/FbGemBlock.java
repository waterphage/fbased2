package com.waterphage.block.models;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class FbGemBlock extends FbBlock{
    public FbGemBlock(Settings settings) {
        super(settings);
    }
    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }
    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1.0F;
    }
    @Override
    public boolean isSideInvisible(BlockState state, BlockState adjacent, Direction side) {
        // если сосед — тот же класс, не прятать грань
        return adjacent.getBlock() == state.getBlock();
    }
}
