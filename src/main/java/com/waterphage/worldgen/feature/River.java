package com.waterphage.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import com.waterphage.meta.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.stateprovider.AbstractNoiseBlockStateProvider;

import java.util.*;

import static com.waterphage.worldgen.feature.River.RiverConfig.RIVER_NOISE;
import static com.waterphage.worldgen.feature.River.RiverConfig.SURFACE;

public class River extends Feature<River.RiverConfig> {
    private static DoublePerlinNoiseSampler noiseSampler;
    private static long seed; private static int ymin;private static int ymax;
    public River(Codec<River.RiverConfig> codec) {super(codec);}

    public static final UnboundedMapCodec<String, RegistryEntry<PlacedFeature>> FB_RIVER_MAP_CODEC =
            Codec.unboundedMap(Codec.STRING, PlacedFeature.REGISTRY_CODEC);
    public record RiverEntry(Map<String, RegistryEntry<PlacedFeature>> features, Double V, Double P,Double Y) {}
    public static final Codec<RiverEntry> FB_RIVER_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    FB_RIVER_MAP_CODEC.fieldOf("valley").forGetter(config -> config.features),
                    Codec.DOUBLE.fieldOf("pn").forGetter(config -> config.V),
                    Codec.DOUBLE.fieldOf("fy").forGetter(config -> config.P),
                    Codec.DOUBLE.fieldOf("py").forGetter(config -> config.Y)
            ).apply(instance, RiverEntry::new)
    );
    public record LakeEntry(Map<String, RegistryEntry<PlacedFeature>> features,
                            Integer depth,Optional<Integer> offset) {}

    public static final Codec<LakeEntry> FB_LAKE_CODEC=RecordCodecBuilder.create(instance ->
            instance.group(
                    FB_RIVER_MAP_CODEC.fieldOf("fill").forGetter(config -> config.features),
                    Codec.INT.fieldOf("depth").forGetter(config -> config.depth),
                    Codec.INT.optionalFieldOf("offset").forGetter(config -> config.offset)
            ).apply(instance, LakeEntry::new)
    );
    public static class RiverConfig implements FeatureConfig {
        public static final Codec<River.RiverConfig> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        PlacedFeature.CODEC.fieldOf("base").forGetter(config -> config.base),
                        FB_RIVER_CODEC.optionalFieldOf("river").forGetter(config -> config.r),
                        FB_LAKE_CODEC.optionalFieldOf("lake").forGetter(config -> config.l)
                ).apply(instance, River.RiverConfig::new));
        private final PlacedFeature base;
        private final Optional<RiverEntry> r;
        private final Optional<LakeEntry> l;
        public static final RegistryKey<DoublePerlinNoiseSampler.NoiseParameters> RIVER_NOISE =
                RegistryKey.of(
                        RegistryKeys.NOISE_PARAMETERS,
                        new Identifier("fbased", "river")
                );
        public static final RegistryKey<DensityFunction> SURFACE =
                RegistryKey.of(
                        RegistryKeys.DENSITY_FUNCTION,
                        new Identifier("fbased", "surface")
                );

        public RiverConfig(PlacedFeature base,Optional<RiverEntry> river,Optional<LakeEntry> l) {
            this.base=base;
            this.r=river;
            this.l=l;
        }
    }
    private void update(FeatureContext<RiverConfig> context, long s){
        {
            RegistryEntry<DoublePerlinNoiseSampler.NoiseParameters> entry =
                    context.getWorld()
                            .toServerWorld()
                            .getRegistryManager()
                            .get(RegistryKeys.NOISE_PARAMETERS)
                            .getEntry(RIVER_NOISE)
                            .orElseThrow();
            noiseSampler =
                    DoublePerlinNoiseSampler.create(
                            new ChunkRandom(new CheckedRandom(s)),
                            entry.value()
                    );
            seed=s;ymin=context.getWorld().getBottomY();ymax=context.getWorld().getHeight();
        }
    }
    private Integer rivercalc(RiverEntry r,int x,int y,int z){
        Double V=Math.pow(-1D*Math.min(Math.abs(noiseSampler.sample(x, y, z)),1D)+1D,r.V);
        double f = (double) Math.pow((ymax-r.P*(y-ymin)) / (ymax),r.Y);
        return Math.toIntExact(Math.round(15D*V*f));
    }

    @Override
    public boolean generate(FeatureContext<RiverConfig> context) {
        RiverConfig c=context.getConfig();
        BlockPos org=context.getOrigin();
        int x=org.getX();int y=org.getY();int z=org.getZ();
        StructureWorldAccess w=context.getWorld();ChunkGenerator g= context.getGenerator();
        Random ran = context.getRandom();
        if (c.r.isPresent()){
            RiverEntry r=c.r.get();
            long s=w.getSeed();
            if (s!=seed){update(context,s);}
            RegistryEntry<PlacedFeature> feature = r.features.get(Integer.toString(rivercalc(r,x,y,z)));
            if (feature != null) {
                feature.value().generate(w, g, ran, org);}
        }

        if (c.l.isPresent()){
            LakeEntry l=c.l.get();
            Integer h=l.depth-(y+l.offset.orElse(0))%(l.depth+1);
            RegistryEntry<PlacedFeature> feature = l.features.get(Integer.toString(h));
            if (feature != null) {feature.value().generate(w, g, ran, org);}
        }
        c.base.generate(w,g,ran,org);
        return true;
    }
    private Double f(int i,List<Double>field){return field.get(i);}
}
