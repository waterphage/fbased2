package com.waterphage.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.waterphage.meta.IntPair;
import com.waterphage.block.models.TechBlock;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.*;

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
                        Codec.INT.fieldOf("max").forGetter(config -> config.max)
                        //Identifier.CODEC.listOf().fieldOf("biomes").forGetter(config -> config.biomes),
                        //Codec.unboundedMap(Codec.INT,
                                        //Codec.unboundedMap(Codec.BOOL, PlacedFeature.REGISTRY_CODEC))
                                //.fieldOf("features")
                                //.forGetter(config -> config.features)
                ).apply(instance, SurfaceConfig::new));
        private Integer min;
        private Integer yT;
        private Integer max;
        private List<Identifier> biomes;
        private Map<Integer,Map<Boolean, RegistryEntry<PlacedFeature>>> features;

        SurfaceConfig(Integer yT,Integer min, Integer max
                      //List<Identifier> biomes,Map<Integer,Map<Boolean,RegistryEntry<PlacedFeature>>> features
        ) {
            this.yT=yT;
            this.max = max;
            this.min = min;
            this.biomes = biomes;
            this.features=features;
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
                        else {entry.setValue(m?18:-18);}
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
    private void getsmooth(
            Map<BlockPos,List<BlockPos>>map,
            Map<BlockPos,List<BlockPos>>edgeS,
            Map<BlockPos,List<BlockPos>>edgeW,
            Map<IntPair,Map<Integer,Integer>>global,Integer x,Integer y,int z,boolean m){
        List<BlockPos> local=new ArrayList<>();
        List<IntPair> search = Arrays.asList(
                new IntPair(x-1,z),
                new IntPair(x+1,z),
                new IntPair(x,z-1),
                new IntPair(x,z+1)
        );
        for (IntPair pos:search){
            Map<Integer,Integer>pairs=global.get(pos);
            if(pairs==null){continue;}
            for(int yl=y-1;yl<=y+1;yl++){
                Integer i=pairs.get(yl);
                if(i==null){continue;}
                if(m?i>=2&&i<=17:i<=-2&&i>=-17){local.add(new BlockPos(pos.first(),yl,pos.second()));}
                else if(m?i>17:i<-17){
                    edgeW.put(new BlockPos(x,y,z),new ArrayList<>());
                }
            }
        }
        map.put(new BlockPos(x,y,z),local);
        if (local.size()<4){edgeS.put(new BlockPos(x,y,z),local);}
    }
    private void getsteep(
            Map<BlockPos,List<BlockPos>>map,
            Map<IntPair,Map<Integer,Integer>>global,Integer x,Integer y,int z,boolean m
    ) {
        List<BlockPos> local = new ArrayList<>();
        List<IntPair> search = Arrays.asList(
                new IntPair(x - 1, z),
                new IntPair(x + 1, z),
                new IntPair(x, z - 1),
                new IntPair(x, z + 1)
        );
        for (IntPair pos : search) {
            Map<Integer, Integer> pairs = global.get(pos);
            if (pairs == null) {continue;}
            Optional<Integer> minY = pairs.entrySet().stream()
                    .filter(e -> e.getKey() >= y && (m ? e.getValue() > 17 : e.getValue() < 17)) // Условия: y >= targetY и i > 17
                    .map(Map.Entry::getKey) // Только y
                    .min(Integer::compareTo); // Берем минимальный y
            if (minY.isPresent()) {
                local.add(new BlockPos(pos.first(),minY.get(),pos.second()));
            }
        }
        map.put(new BlockPos(x,y,z),local);
    }
    Map<String,Map<BlockPos,List<BlockPos>>> neighbours(Map<IntPair,Map<Integer,Integer>>global){

        Map<String,Map<BlockPos,List<BlockPos>>> map=new HashMap<>();
        Map<BlockPos,List<BlockPos>> neighboursSF=new HashMap<>();
        Map<BlockPos,List<BlockPos>> edgeSF=new HashMap<>();
        Map<BlockPos,List<BlockPos>> neighboursSC=new HashMap<>();
        Map<BlockPos,List<BlockPos>> edgeSC=new HashMap<>();

        Map<BlockPos,List<BlockPos>> neighboursWF=new HashMap<>();
        Map<BlockPos,List<BlockPos>> edgeWF=new HashMap<>();
        Map<BlockPos,List<BlockPos>> neighboursWC=new HashMap<>();
        Map<BlockPos,List<BlockPos>> edgeWC=new HashMap<>();

        for(Map.Entry<IntPair,Map<Integer,Integer>> XZ:global.entrySet()){
            IntPair xz=XZ.getKey();
            Integer x=xz.first();
            Integer z=xz.second();
            Map<Integer,Integer> positions=XZ.getValue();
            for (Map.Entry<Integer,Integer> point:positions.entrySet()){
                int y=point.getKey();
                int i=point.getValue();
                if(i>17){
                    getsteep(neighboursWF,global,x,y,z,true);
                }else if(i>=1){
                    getsmooth(neighboursSF,edgeSF,edgeWF,global,x,y,z,true);
                }else if(i>-18){
                    getsmooth(neighboursSC,edgeSC,edgeWC,global,x,y,z,false);
                }else{
                    getsteep(neighboursWC,global,x,y,z,false);
                }
            }
        }
        for (BlockPos pos:edgeWF.keySet()){
            edgeWF.put(pos,neighboursWF.get(pos));
        }
        for (BlockPos pos:edgeWC.keySet()){
            edgeWC.put(pos,neighboursWC.get(pos));
        }
        map.put("neighbours_s_f",neighboursSF);
        map.put("neighbours_s_c",neighboursSC);
        map.put("neighbours_w_f",neighboursWF);
        map.put("neighbours_w_c",neighboursWC);

        map.put("edge_s_f",edgeSF);
        map.put("edge_s_c",edgeSC);
        map.put("edge_w_f",edgeWF);
        map.put("edge_w_c",edgeWC);
        return map;
    }
    private void writesurface(
            Map<IntPair,Map<Integer,Integer>>global,
            Map<BlockPos,List<BlockPos>> map,
            Map<BlockPos,List<BlockPos>> edge,
            boolean m){
        int i=m?17:-17;
        for (int k=0;k<16;k++){
            Set<BlockPos> cache=new HashSet<>();
            for (List<BlockPos> neighbours:edge.values()){cache.addAll(neighbours);}
            for (BlockPos pos:cache){
                if (edge.keySet().contains(pos)){
                    edge.remove(pos);
                    IntPair xz=new IntPair(pos.getX(),pos.getZ());
                    int n=global.get(xz).get(pos.getY());
                    if(m?n<i:n>i){
                        Map<Integer,Integer> point=global.get(xz);
                        point.put(pos.getY(),i);
                        global.put(xz,point);
                    }
                }else {
                    edge.put(pos,map.get(pos));
                }
                i=m?17-k:-17+k;
            }
        }
    }
    private void localcaching(Map<IntPair,Map<Integer,Integer>>global,
                              Map<String,Map<BlockPos,List<BlockPos>>> neighbours,
                              int xi,int zi){
        Map<BlockPos,List<BlockPos>> neighboursSF=neighbours.get("neighbours_s_f");
        Map<BlockPos,List<BlockPos>> neighboursSC=neighbours.get("neighbours_s_c");
        Map<BlockPos,List<BlockPos>> neighboursWF=neighbours.get("neighbours_w_f");
        Map<BlockPos,List<BlockPos>> neighboursWC=neighbours.get("neighbours_w_c");

        Map<BlockPos,List<BlockPos>> edgeSF=neighbours.get("edge_s_f");
        Map<BlockPos,List<BlockPos>> edgeSC=neighbours.get("edge_s_c");
        Map<BlockPos,List<BlockPos>> edgeWF=neighbours.get("edge_w_f");
        Map<BlockPos,List<BlockPos>> edgeWC=neighbours.get("edge_w_c");

        writesurface(global,neighboursSF,edgeSF,true);
        writesurface(global,neighboursSC,edgeSC,false);
    }
    private List<Pair<BlockPos,Integer>>placer(StructureWorldAccess world,int xi,int zi,int yT){
        List<Pair<BlockPos,Integer>>goal=new ArrayList<>();
        Map<IntPair,Map<Integer,Integer>>global=global(world,xi,zi,yT);
        globalcaching(global,xi,zi);
        Map<String,Map<BlockPos,List<BlockPos>>> neighbours=neighbours(global);
        localcaching(global,neighbours,xi,zi);
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
    private void test(int i,BlockPos pos,StructureWorldAccess world){
        BlockState block;
        switch (i){
            case 2:
                block=Registries.BLOCK.get(new Identifier("minecraft:black_concrete")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 3:
                block=Registries.BLOCK.get(new Identifier("minecraft:brown_concrete")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 4:
                block=Registries.BLOCK.get(new Identifier("minecraft:red_concrete")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 5:
                block=Registries.BLOCK.get(new Identifier("minecraft:orange_concrete")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 6:
                block=Registries.BLOCK.get(new Identifier("minecraft:yellow_concrete")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 7:
                block=Registries.BLOCK.get(new Identifier("minecraft:lime_concrete")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 8:
                block=Registries.BLOCK.get(new Identifier("minecraft:green_concrete")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 9:
                block=Registries.BLOCK.get(new Identifier("minecraft:cyan_concrete")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 10:
                block=Registries.BLOCK.get(new Identifier("minecraft:light_blue_concrete")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 11:
                block=Registries.BLOCK.get(new Identifier("minecraft:blue_concrete")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 12:
                block=Registries.BLOCK.get(new Identifier("minecraft:purple_concrete")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 13:
                block=Registries.BLOCK.get(new Identifier("minecraft:magenta_concrete")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 14:
                block=Registries.BLOCK.get(new Identifier("minecraft:pink_concrete")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 15:
                block=Registries.BLOCK.get(new Identifier("minecraft:white_concrete")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 16:
                block=Registries.BLOCK.get(new Identifier("minecraft:light_gray_concrete")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 17:
                block=Registries.BLOCK.get(new Identifier("minecraft:gray_concrete")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
        }
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
            if(pos.getY()>40){
                test(Math.abs(i),pos,world);
            }


            //config.defaultFeature.value().generateUnregistered(world, chunkGenerator, random,pos);
        }
        return true;
    }
}