package com.waterphage.block.models;

import net.minecraft.block.BlockState;
import net.minecraft.block.WallBlock;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class FbGemWall extends WallBlock {
    public FbGemWall(Settings settings) {
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

}
