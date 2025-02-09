package com.waterphage.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.waterphage.block.models.TechBlockEntity;
import com.waterphage.meta.IntPair;
import com.waterphage.block.models.TechBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.*;
import java.util.stream.Stream;

public class Surface extends Feature<Surface.SurfaceConfig> {

    public Surface(Codec<SurfaceConfig> codec) {
        super(codec);
    }

    //Generates features based on the biome and configuration conditions.
    public static class SurfaceConfig implements FeatureConfig {
        public static final Codec<SurfaceConfig> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Codec.INT.fieldOf("tech_Y").forGetter(config -> config.yT),
                        Codec.INT.fieldOf("min").forGetter(config -> config.min),
                        Codec.INT.fieldOf("max").forGetter(config -> config.max),
                        PlacedFeature.REGISTRY_CODEC.fieldOf("default").forGetter(config -> config.defaultFeature)
                ).apply(instance, SurfaceConfig::new)
        );
        private Integer min;
        private Integer yT;
        private Integer max;
        private RegistryEntry<PlacedFeature> defaultFeature;
        SurfaceConfig(Integer yT,Integer min, Integer max,RegistryEntry<PlacedFeature> defaultFeature) {
            this.yT=yT;
            this.max = max;
            this.min = min;
            this.defaultFeature=defaultFeature;
        }
    }
    private boolean simplech(Map<IntPair,Map<Integer,Integer>>global,boolean m,int xf,int yo,int zf){
        List<IntPair> search = Arrays.asList(
                new IntPair(xf-1,zf),
                new IntPair(xf+1,zf),
                new IntPair(xf,zf-1),
                new IntPair(xf,zf+1)
        );
        for (IntPair pos:search){
            Map<Integer,Integer>pairs=global.get(pos);
            if (pairs==null){return false;}
            if(!checklocal(pairs,yo-1,yo+1,m)){return false;}
        }
        return true;
    }
    private boolean checklocal(Map<Integer,Integer>pairs,int ymin,int ymax,boolean m){
        return pairs.entrySet().stream()
                .anyMatch(entry -> {
                    int y = entry.getKey();
                    int i = entry.getValue();
                    return y >= ymin && y <= ymax && ((m && i > 0) || (!m && i < 0));
                });
    }
    private void globalcaching(Map<IntPair,Map<Integer,Integer>>global,int xi,int zi){
        for (int x=-16;x<=31;x++){
            for (int z=-16;z<=31;z++) {
                IntPair key=new IntPair(xi+x,zi+z);
                Map<Integer,Integer> surf=global.get(key);
                for (Map.Entry<Integer, Integer> entry : surf.entrySet()){
                    int y=entry.getKey();
                    int i=entry.getValue();
                    boolean m=i>0;
                    if (Math.abs(i)==1){
                        if(simplech(global,m,xi+x,y,zi+z)){entry.setValue(m?2:-2);}
                        else {entry.setValue(m?17:-17);}
                    }
                }
                global.put(key,surf);
            }
        }
    }
    private Map<IntPair,Map<Integer,Integer>> global(StructureWorldAccess world,int xi,int zi,int yT){
        Map<IntPair,Map<Integer,Integer>>global=new HashMap<>();
        BlockPos.Mutable src=new BlockPos.Mutable(0,yT,0);
        for (int x=-16;x<=31;x++){
            for (int z=-16;z<=31;z++){
                int xf=x+xi;int zf=z+zi;
                src.setX(xf).setZ(zf);
                Map<Integer,Integer>pair=new HashMap<>();
                for (int yw=yT;;yw+=2){
                    if(!world.getBlockState(src.setY(yw)).contains(TechBlock.CACHE)){break;}
                    int y=world.getBlockState(src.setY(yw)).get(TechBlock.CACHE);
                    int i=world.getBlockState(src.setY(yw+1)).get(TechBlock.CACHE);
                    pair.put(y,i-33);
                }
                IntPair xz=new IntPair(xf,zf);
                global.put(xz,pair);
            }
        }
        return global;
    }

    private boolean checkfloor(Map<Integer,Integer>global,int yo,boolean m){
        if (Optional.ofNullable(global.get(yo)).filter(val -> (m && val > 0) || (!m && val < 0)).isPresent()) {
            return true;
        }
        return false;
    }

    private int surfacecheck(Map<IntPair,Map<Integer,Integer>>global,int xi,int yo,int zi,boolean m){
        int i=1;
        for (;i<=15;i++){
            if(!checkfloor(global.get(new IntPair(xi,zi)),yo-i-2,!m)){return i+1;}
            if(!checklocal(global.get(new IntPair(xi+i,zi)),yo-i-1,yo+i+1,m)){return i+1;}
            if(!checklocal(global.get(new IntPair(xi-i,zi)),yo-i-1,yo+i+1,m)){return i+1;}
            if(!checklocal(global.get(new IntPair(xi,zi-i)),yo-i-1,yo+i+1,m)){return i+1;}
            if(!checklocal(global.get(new IntPair(xi,zi+i)),yo-i-1,yo+i+1,m)){return i+1;}
        }
        return i+1;
    }
    private void localcaching(Map<IntPair,Map<Integer,Integer>>global,int xi,int zi){
        for (int x=0;x<=15;x++){
            for (int z=0;z<=15;z++) {
                IntPair key=new IntPair(xi+x,zi+z);
                Map<Integer,Integer> surf=global.get(key);
                for (Map.Entry<Integer, Integer> entry : surf.entrySet()){
                    int y=entry.getKey();
                    int i=entry.getValue();
                    boolean m=i>0;
                    int ic=Math.abs(i);
                    if (ic==2){
                        entry.setValue(surfacecheck(global,xi+x,y,zi+z,m));
                    }else if (ic==17){
                        //entry.setValue(slopecheck(global,xi+x,y,zi+z,i,m));
                    }
                }
                global.put(key,surf);
            }
        }
    }
    private List<Pair<BlockPos,Integer>>placer(StructureWorldAccess world,int xi,int zi,int yT){
        List<Pair<BlockPos,Integer>>goal=new ArrayList<>();
        Map<IntPair,Map<Integer,Integer>>global=global(world,xi,zi,yT);
        globalcaching(global,xi,zi);
        localcaching(global,xi,zi);
        BlockState key = Registries.BLOCK.get(new Identifier("fbased:surface_cache")).getDefaultState();
        for (int x=0;x<=15;x++){
            for (int z=0;z<=15;z++) {
                Map<Integer,Integer> ydata=global.get(new IntPair(xi+x,zi+z));
                int yw=yT;
                for (Map.Entry<Integer, Integer> entry : ydata.entrySet()){
                    world.setBlockState(new BlockPos(xi+x,yw,zi+z),key.with(TechBlock.CACHE, entry.getKey()),3);
                    world.setBlockState(new BlockPos(xi+x,yw+1,zi+z),key.with(TechBlock.CACHE, entry.getValue()+33),3);
                    goal.add(new Pair<>(new BlockPos(xi+x,entry.getKey(),zi+z),entry.getValue()));
                    yw+=2;
                }
            }
        }
        return goal;
    }
    @Override
    public boolean generate(FeatureContext<SurfaceConfig> context) {
        SurfaceConfig config = context.getConfig();
        BlockPos origin = context.getOrigin();
        StructureWorldAccess world = context.getWorld();
        Random random = context.getRandom();
        ChunkGenerator chunkGenerator = context.getGenerator();
        int xi=origin.getX();int zi=origin.getZ();
        BlockPos.Mutable or = new BlockPos.Mutable();
        List<Pair<BlockPos,Integer>>placer=placer(world,xi,zi,config.yT);
        for (Pair<BlockPos,Integer> entry:placer){
            BlockPos pos = entry.getLeft();
            int i = entry.getRight();
            if (i>=config.min&&i<=config.max){
                config.defaultFeature.value().generateUnregistered(world, chunkGenerator, random,pos);
            }
        }
        return true;
    }
}