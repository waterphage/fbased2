package com.waterphage.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.List;
import java.util.stream.Stream;

public class Biome extends Feature<Biome.BiomeConfig> {

    public Biome(Codec<BiomeConfig> codec) {
        super(codec);
    }

    //Generates features based on the biome and configuration conditions.

    @Override
    public boolean generate(FeatureContext<BiomeConfig> context) {
        BiomeConfig config = context.getConfig();
        BlockPos origin = context.getOrigin();
        StructureWorldAccess world = context.getWorld();
        Random random = context.getRandom();
        ChunkGenerator chunkGenerator = context.getGenerator();

        int initialY = origin.getY();
        int x = origin.getX();
        int z = origin.getZ();

        // Iterate through each biome entry in the configuration
        for (BiomeConfig.BiomeEntry entry : config.features) {
            int y = parseHeight(entry.probe, initialY);
            BlockPos.Mutable mutablePos = new BlockPos.Mutable(x, y, z);

            Identifier biomeId = world.getBiome(mutablePos).getKey().orElseThrow().getValue();
            if (entry.biome.contains(biomeId)) {
                return entry.generate(world, chunkGenerator, random, origin);
            }
        }

        // Generate the default feature if no biome-specific features matched
        return config.defaultFeature.value().generateUnregistered(world, chunkGenerator, random, origin);
    }

    //Parses the height from the provided string or uses the default value if parsing fails.

    private int parseHeight(String probe, int defaultY) {
        try {
            return Integer.parseInt(probe);
        } catch (NumberFormatException e) {
            return defaultY;
        }
    }

    //Configuration class for the Biome feature.

    public static class BiomeConfig implements FeatureConfig {
        public static final Codec<BiomeConfig> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        BiomeEntry.CODEC.listOf().fieldOf("features").forGetter(config -> config.features),
                        PlacedFeature.REGISTRY_CODEC.fieldOf("default").forGetter(config -> config.defaultFeature)
                ).apply(instance, BiomeConfig::new)
        );

        public final List<BiomeEntry> features;
        public final RegistryEntry<PlacedFeature> defaultFeature;

        public BiomeConfig(List<BiomeEntry> features, RegistryEntry<PlacedFeature> defaultFeature) {
            this.features = features;
            this.defaultFeature = defaultFeature;
        }

        @Override
        public Stream<ConfiguredFeature<?, ?>> getDecoratedFeatures() {
            return features.stream().flatMap(entry -> ((PlacedFeature) entry.feature.value()).getDecoratedFeatures());
        }

        //Represents an individual biome entry in the configuration.

        public static class BiomeEntry {
            public static final Codec<BiomeEntry> CODEC = RecordCodecBuilder.create(
                    instance -> instance.group(
                            PlacedFeature.REGISTRY_CODEC.fieldOf("placed_feature").forGetter(config -> config.feature),
                            Identifier.CODEC.listOf().fieldOf("biomes").forGetter(config -> config.biome),
                            Codec.STRING.fieldOf("y").forGetter(config -> config.probe)
                    ).apply(instance, BiomeEntry::new)
            );

            public final RegistryEntry<PlacedFeature> feature;
            public final List<Identifier> biome;
            public final String probe;

            public BiomeEntry(RegistryEntry<PlacedFeature> feature, List<Identifier> biome, String probe) {
                this.feature = feature;
                this.biome = biome;
                this.probe = probe;
            }

            //Generates the feature associated with this entry.

            public boolean generate(StructureWorldAccess world, ChunkGenerator chunkGenerator, Random random, BlockPos pos) {
                return this.feature.value().generateUnregistered(world, chunkGenerator, random, pos);
            }
        }
    }
}