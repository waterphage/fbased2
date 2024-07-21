package com.waterphage.block.worldgen;

import com.mojang.serialization.Codec;
import com.waterphage.Fbased;
import com.waterphage.block.worldgen.feature.*;
import com.waterphage.block.worldgen.placers.FbasedPlacers;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class ModChunk extends ChunkGenerator {


    public ModChunk(BiomeSource biomeSource, Function<RegistryEntry<Biome>, GenerationSettings> generationSettingsGetter) {
        super(biomeSource, generationSettingsGetter);
    }

    /**
     * @return
     */
    @Override
    protected Codec<? extends ChunkGenerator> getCodec() {
        return null;
    }

    /**
     * @param chunkRegion
     * @param seed
     * @param noiseConfig
     * @param biomeAccess
     * @param structureAccessor
     * @param chunk
     * @param carverStep
     */
    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {

    }

    /**
     * @param region
     * @param structures
     * @param noiseConfig
     * @param chunk
     */
    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
    }

    /**
     * @param region
     */
    @Override
    public void populateEntities(ChunkRegion region) {

    }

    /**
     * @return
     */
    @Override
    public int getWorldHeight() {
        return 0;
    }

    /**
     * @param executor
     * @param blender
     * @param noiseConfig
     * @param structureAccessor
     * @param chunk
     * @return
     */
    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        return null;
    }

    /**
     * @return
     */
    @Override
    public int getSeaLevel() {
        return 0;
    }

    /**
     * @return
     */
    @Override
    public int getMinimumY() {
        return 0;
    }

    /**
     * @param x
     * @param z
     * @param heightmap
     * @param world
     * @param noiseConfig
     * @return
     */
    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        return 0;
    }

    /**
     * @param x
     * @param z
     * @param world
     * @param noiseConfig
     * @return
     */
    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        return null;
    }

    /**
     * @param text
     * @param noiseConfig
     * @param pos
     */
    @Override
    public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {

    }

    public static void registerModChunk() {
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "geo"), new GeoFeature(GeoFeature.GeoFeatureConfig.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "test"), new TestF(TestF.TestFConfig.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "test2"), new TestF2(TestF2.TestF2Config.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "mul"), new Multiple(Multiple.MultipleConfig.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "disk"), new Disk(Disk.DiskConfig.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "fossil"), new Fossil(Fossil.FossilConfig.CODEC));
        FbasedPlacers.registerplacers();
        Registry.register(Registries.CHUNK_GENERATOR, new Identifier(Fbased.MOD_ID, "fbasedgen"), ModChunk.CODEC);
    }
}