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

        for (MultipleConfig.MultipleEntry entry : config.features) {
            return entry.generate(world, chunkGenerator, random, pos);
        }
        return false;
    }

    public static class MultipleConfig implements FeatureConfig {
        public static final Codec<MultipleConfig> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        MultipleEntry.CODEC.listOf().fieldOf("features").forGetter(config -> config.features)
                ).apply(instance, MultipleConfig::new));

        public final List<MultipleEntry> features;

        public MultipleConfig(List<MultipleEntry> features) {
            this.features = features;
        }

        @Override
        public Stream<ConfiguredFeature<?, ?>> getDecoratedFeatures() {
            return features.stream().flatMap(entry -> ((PlacedFeature) entry.feature.value()).getDecoratedFeatures());
        }

        public static class MultipleEntry {
            public static final Codec<MultipleEntry> CODEC = RecordCodecBuilder.create(
                    instance -> instance.group(
                            PlacedFeature.REGISTRY_CODEC.fieldOf("entry").forGetter(config -> config.feature)
                    ).apply(instance, MultipleEntry::new));

            public final RegistryEntry<PlacedFeature> feature;

            public MultipleEntry(RegistryEntry<PlacedFeature> feature) {
                this.feature = feature;
            }

            public boolean generate(StructureWorldAccess world, ChunkGenerator chunkGenerator, Random random, BlockPos pos) {
                return ((PlacedFeature) this.feature.value()).generate(world, chunkGenerator, random, pos);
            }
        }
    }
}