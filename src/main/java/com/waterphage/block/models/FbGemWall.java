package com.waterphage.block.models;

import com.waterphage.block.ModMaterials;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallBlock;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.biome.Biome;

public class FbGemWall extends WallBlock {
    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        RegistryEntry<Biome> b= world.getBiome(pos);
        ModMaterials.climatizer(state,world,pos,b);
    }
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
    public static final TagKey<Block> GEMS = TagKey.of(RegistryKeys.BLOCK, new Identifier("fbased", "gems"));
    @Override
    public boolean isSideInvisible(BlockState state, BlockState adjacent, Direction side) {
        // если сосед — тот же класс, не прятать грань
        return adjacent.isIn(GEMS);
    }
    private int l=0;
    @Override
    public int getOpacity(BlockState state, BlockView world, BlockPos pos) {
        return l;
    }
    public FbGemWall(Settings settings, int l) {super(settings);this.l=l;}

}
