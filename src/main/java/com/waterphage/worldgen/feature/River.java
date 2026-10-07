package com.waterphage.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import com.waterphage.meta.ChunkExtension;
import com.waterphage.meta.Storage;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.*;

public class River extends Feature<River.RiverConfig> {
    public River(Codec<River.RiverConfig> codec) {super(codec);}

    public static final UnboundedMapCodec<Integer, RegistryEntry<PlacedFeature>> FB_RIVER_MAP_CODEC = Codec.unboundedMap(Codec.INT, PlacedFeature.REGISTRY_CODEC);
    public static class RiverConfig implements FeatureConfig {
        public static final Codec<River.RiverConfig> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        PlacedFeature.CODEC.fieldOf("base").forGetter(config -> config.base),
                        FB_RIVER_MAP_CODEC.fieldOf("valley").forGetter(config -> config.features),
                        DensityFunction.CODEC.fieldOf("noise").forGetter(config -> config.noise),
                        Codec.DOUBLE.optionalFieldOf("noise_power").forGetter(config -> config.p),
                        Codec.DOUBLE.optionalFieldOf("argument_power").forGetter(config -> config.P)
                ).apply(instance, River.RiverConfig::new));
        private final PlacedFeature base;
        private final Map<Integer, RegistryEntry<PlacedFeature>> features;
        private final DensityFunction noise;
        private final Optional<Double> p;
        private final Optional<Double> P;
        public RiverConfig(PlacedFeature base,Map<Integer, RegistryEntry<PlacedFeature>> features, DensityFunction noise, Optional<Double>p, Optional<Double>P) {
            this.base=base;
            this.features = features;
            this.noise=noise;
            this.p=p;this.P=P;
        }
    }
    private List<Double> field=new ArrayList<>();
    @Override
    public boolean generate(FeatureContext<RiverConfig> context) {
        BlockPos org=context.getOrigin();
        int x=org.getX();int y=org.getY();int z=org.getZ();
        field.add(probe(x,y,z));
        field.add(probe(x-4,y,z));
        field.add(probe(x+4,y,z));
        field.add(probe(x,y-8,z));
        field.add(probe(x,y+8,z));
        field.add(probe(x,y,z-4));
        field.add(probe(x,y,z+4));
        Double dFdx=(f(2)-f(1))/8D;Double dFdy=(f(4)-f(3))/16D;Double dFdz=(f(6)-f(5))/8D;
        Double ф=Math.abs(-dFdy/Math.sqrt(dFdx*dFdx+dFdy*dFdy+dFdz*dFdz));
        Double к = Math.max(1D,Math.max((f(1)+f(2)-2D*f(0))/16D,(f(6)+f(5)-2D*f(0))/16D));
        RiverConfig c=context.getConfig();
        Double V=(c.noise.sample(new DensityFunction.NoisePos() {
            @Override public int blockX() {return x;}
            @Override public int blockY() {return y;}
            @Override public int blockZ() {return z;}
        }))+1.0D;
        StructureWorldAccess w=context.getWorld();ChunkGenerator g= context.getGenerator();
        Random r = context.getRandom();
        Integer id= Math.toIntExact(Math.round(15D*(Math.pow(Math.abs(V),c.p.orElse(1D))*Math.pow(ф/к,c.P.orElse(1D)))));
        c.features.get(id).value().generate(w,g,r,org);field.clear();
        c.base.generate(w,g,r,org);
        return true;
    }
    private Double f(int i){return field.get(i);}
    private Double probe(int x,int y,int z){
        return Storage.NoiseType.FIN.getNoise(Storage.params()).sample(new DensityFunction.NoisePos() {
            @Override public int blockX() {return x;}
            @Override public int blockY() {return y;}
            @Override public int blockZ() {return z;}
        });
    }
}
