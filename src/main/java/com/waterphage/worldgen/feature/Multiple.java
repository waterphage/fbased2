package com.waterphage.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.noise.NoiseRouter;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;

import java.util.List;
import java.util.stream.Stream;

public class Multiple extends Feature<Multiple.MultipleConfig> {
    public Multiple(Codec<MultipleConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<MultipleConfig> context) {
        MultipleConfig config = context.getConfig();
        BlockPos pos = context.getOrigin();
        StructureWorldAccess world = context.getWorld();
        Random random = context.getRandom();
        ChunkGenerator chunkGenerator = context.getGenerator();
        for (RegistryEntry<PlacedFeature> entry : config.features) {
            entry.value().generateUnregistered(world, chunkGenerator, random, pos);
        }
        return false;
    }

    public static class MultipleConfig implements FeatureConfig {
        public static final Codec<MultipleConfig> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        PlacedFeature.REGISTRY_CODEC.listOf().fieldOf("features").forGetter(config -> config.features)
                ).apply(instance, MultipleConfig::new));

        public final List<RegistryEntry<PlacedFeature>> features;

        public MultipleConfig(List<RegistryEntry<PlacedFeature>> features) {
            this.features = features;
        }

        @Override
        public Stream<ConfiguredFeature<?, ?>> getDecoratedFeatures() {
            return features.stream().flatMap(entry -> entry.value().getDecoratedFeatures());
        }
    }
}