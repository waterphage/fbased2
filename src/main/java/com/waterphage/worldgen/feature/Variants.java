package com.waterphage.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.noise.NoiseRouter;

import java.rmi.registry.Registry;
import java.util.Map;

public class Variants extends Feature<Variants.VariantsConfig> {
    public Variants(Codec<VariantsConfig> codec){super(codec);}

    public static class VariantsConfig implements FeatureConfig {
        public static final Codec<VariantsConfig> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Codec.INT.fieldOf("yT").forGetter(config -> config.yT),
                        Codec.unboundedMap(Codec.STRING, PlacedFeature.REGISTRY_CODEC).fieldOf("domain").forGetter(config -> config.domain),
                        PlacedFeature.REGISTRY_CODEC.fieldOf("default").forGetter(config -> config.def)
                ).apply(instance, VariantsConfig::new));
        private int yT;
        private Map<String, RegistryEntry<PlacedFeature>> domain;
        private RegistryEntry<PlacedFeature> def;
        VariantsConfig(int yT, Map<String, RegistryEntry<PlacedFeature>>domain,RegistryEntry<PlacedFeature> def) {
            this.yT = yT;
            this.domain=domain;
            this.def=def;
        }

    }
    @Override
    public boolean generate(FeatureContext<VariantsConfig> context) {
        StructureWorldAccess world=context.getWorld();
        ChunkGenerator gen= context.getGenerator();
        Random r=context.getRandom();
        BlockPos.Mutable origin=context.getOrigin().mutableCopy();
        String id=world.getRegistryManager().get(RegistryKeys.BIOME).getId(world.getBiome(origin.setY(context.getConfig().yT)).value()).toString();
        RegistryEntry<PlacedFeature> feature=context.getConfig().domain.get(id);
        if(feature==null)feature=context.getConfig().def;
        return feature.value().generateUnregistered(world,gen,r,origin);
    }
}
