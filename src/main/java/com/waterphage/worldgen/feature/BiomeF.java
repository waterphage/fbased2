package com.waterphage.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class BiomeF extends Feature<BiomeF.BiomeConfig> {

    public BiomeF(Codec<BiomeConfig> codec) {
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

        int y = origin.getY();
        int x = origin.getX();
        int z = origin.getZ();

        // Iterate through each biome entry in the configuration
        for (BiomeConfig.BiomeEntry entry : config.features) {
            BlockPos.Mutable mutablePos = new BlockPos.Mutable(x, entry.probe.orElse(y), z);
            RegistryEntry<Biome> biome = world.getBiome(mutablePos);

            if (entry.biome.isPresent()) {
                Identifier biomeId = biome.getKey().orElseThrow().getValue();

                if (entry.biome.get().contains(biomeId)) {
                    return entry.generate(world, chunkGenerator, random, origin);
                }
            }

            if (entry.biomet.isPresent()) {
                TagKey<Biome> tag = TagKey.of(RegistryKeys.BIOME, entry.biomet.get());
                if (biome.isIn(tag)) {
                    return entry.generate(world, chunkGenerator, random, origin);
                }
            }
        }

        // Generate the default feature if no biome-specific features matched
        return config.defaultFeature.value().generateUnregistered(world, chunkGenerator, random, origin);
    }

    //Configuration class for the Biome feature.

    public static class BiomeConfig implements FeatureConfig {
        public static final Codec<BiomeConfig> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        BiomeEntry.CODEC.listOf().fieldOf("features").forGetter(config -> config.features),
                        PlacedFeature.REGISTRY_CODEC.fieldOf("default").forGetter(config -> config.defaultFeature)
                ).apply(instance, BiomeConfig::new));
        public final List<BiomeEntry> features;
        public final RegistryEntry<PlacedFeature> defaultFeature;
        public BiomeConfig(List<BiomeEntry> features, RegistryEntry<PlacedFeature> defaultFeature) {
            this.features = features;
            this.defaultFeature = defaultFeature;
        }
        @Override
        public Stream<ConfiguredFeature<?, ?>> getDecoratedFeatures() {
            return features.stream().flatMap(entry -> ((PlacedFeature) entry.feature.value()).getDecoratedFeatures()); }
        //Represents an individual biome entry in the configuration.
        public static class BiomeEntry {
            public static final Codec<BiomeEntry> CODEC = RecordCodecBuilder.create(
                    instance -> instance.group(
                            PlacedFeature.REGISTRY_CODEC.fieldOf("placed_feature").forGetter(config -> config.feature),
                            Identifier.CODEC.listOf().optionalFieldOf("biomes").forGetter(config -> config.biome),
                            Identifier.CODEC.optionalFieldOf("biome_tag").forGetter(config -> config.biomet),
                            Codec.INT.optionalFieldOf("y").forGetter(config -> config.probe)
                    ).apply(instance, BiomeEntry::new));
            public final RegistryEntry<PlacedFeature> feature;
            public final Optional<List<Identifier>> biome;
            public final Optional<Identifier> biomet;
            public final Optional<Integer> probe;
            public BiomeEntry(RegistryEntry<PlacedFeature> feature, Optional<List<Identifier>> biome, Optional<Identifier> biomet, Optional<Integer> probe)
            { this.feature = feature;
                this.biome = biome;
                this.biomet = biomet;
                this.probe = probe;
            }
            //Generates the feature associated with this entry.
            public boolean generate(StructureWorldAccess world, ChunkGenerator chunkGenerator, Random random, BlockPos pos) { return this.feature.value().generateUnregistered(world, chunkGenerator, random, pos); }
        }
    }
}