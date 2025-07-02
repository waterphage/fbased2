package com.waterphage.block.models;

import net.minecraft.block.BlockState;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class FbGemSlb extends FbSlbBlock{
    public FbGemSlb(Settings settings, SoundEvent blockSoundEvent) {
        super(settings, blockSoundEvent);
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
