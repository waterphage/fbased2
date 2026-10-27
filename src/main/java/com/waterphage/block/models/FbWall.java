package com.waterphage.block.models;

import com.waterphage.block.ModMaterials;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallBlock;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;

public class FbWall extends WallBlock {
    public FbWall(Settings settings) {
        super(settings);
    }
    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        RegistryEntry<Biome> b= world.getBiome(pos);
        ModMaterials.climatizer(state,world,pos,b);
    }
}
