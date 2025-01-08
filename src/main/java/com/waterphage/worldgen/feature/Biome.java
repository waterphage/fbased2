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

    @Override
    public boolean generate(FeatureContext<BiomeConfig> context) {
        BiomeConfig config = context.getConfig();
        BlockPos pos = context.getOrigin();
        StructureWorldAccess world = context.getWorld();
        Random random = context.getRandom();
        ChunkGenerator chunkGenerator = context.getGenerator();
        int yi=pos.getY();int xi=pos.getX();int zi=pos.getZ();
        boolean result=false;
        for (BiomeConfig.BiomeEntry entry : config.features) {
            int yo=yi;
            try {
                yo = Integer.parseInt(entry.probe);
            } catch (NumberFormatException nfe) {}
            BlockPos.Mutable pos1 = new BlockPos.Mutable(xi,yo,zi);
            Identifier id = world.getBiome(pos1).getKey().get().getValue();
            if (entry.biome.contains(id)) {
                result |= entry.generate(world, chunkGenerator, random, pos);
                return result;
            }
        }
        result |= config.def.generate(world, chunkGenerator, random, pos);
        return result;
    }

    public static class BiomeConfig implements FeatureConfig {
        public static final Codec<BiomeConfig> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        BiomeEntry.CODEC.listOf().fieldOf("features").forGetter(config -> config.features),
                        Multiple.MultipleConfig.MultipleEntry.CODEC.fieldOf("default").forGetter(config -> config.def)
                ).apply(instance, BiomeConfig::new));

        public final List<BiomeEntry> features;
        public final Multiple.MultipleConfig.MultipleEntry def;

        public BiomeConfig(List<BiomeEntry> features,Multiple.MultipleConfig.MultipleEntry def) {
            this.features = features;
            this.def = def;
        }

        @Override
        public Stream<ConfiguredFeature<?, ?>> getDecoratedFeatures() {
            return features.stream().flatMap(entry -> ((PlacedFeature) entry.feature.value()).getDecoratedFeatures());
        }

        public static class BiomeEntry {
            public static final Codec<BiomeEntry> CODEC = RecordCodecBuilder.create(
                    instance -> instance.group(
                            PlacedFeature.REGISTRY_CODEC.fieldOf("placed_feature").forGetter(config -> config.feature),
                            Identifier.CODEC.listOf().fieldOf("biomes").forGetter(config -> config.biome),
                            Codec.STRING.fieldOf("y").forGetter(config -> config.probe)
                    ).apply(instance, BiomeEntry::new));

            public final RegistryEntry<PlacedFeature> feature;
            public final List<Identifier> biome;
            public final String probe;

            public BiomeEntry(RegistryEntry<PlacedFeature> feature, List<Identifier> biome,String probe) {
                this.feature = feature;
                this.biome = biome;
                this.probe = probe;
            }
            public boolean generate(StructureWorldAccess world, ChunkGenerator chunkGenerator, Random random, BlockPos pos) {
                return ((PlacedFeature) this.feature.value()).generateUnregistered(world, chunkGenerator, random, pos);
            }
        }
    }
}