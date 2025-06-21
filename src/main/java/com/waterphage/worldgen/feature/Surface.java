package com.waterphage.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.waterphage.block.models.TechBlockEntity;
import com.waterphage.meta.ChunkExtension;
import com.waterphage.meta.IntPair;
import com.waterphage.block.models.TechBlock;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Surface extends Feature<Surface.SurfaceConfig> {

    private static String MODE;
    private static Map<IntPair,TreeMap<Integer,Integer>> GLOBAL =new HashMap<>();
    private static Map<BlockPos,List<BlockPos>> mapSF=new HashMap<>();
    private static Map<BlockPos,List<BlockPos>> inSF=new HashMap<>();
    private static Map<BlockPos,List<BlockPos>> outSF=new HashMap<>();

    private static Map<BlockPos,List<BlockPos>> mapSC=new HashMap<>();
    private static Map<BlockPos,List<BlockPos>> inSC=new HashMap<>();
    private static Map<BlockPos,List<BlockPos>> outSC=new HashMap<>();
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
                        Codec.STRING.fieldOf("edge_mode").forGetter(config -> config.mode)
                        //Identifier.CODEC.listOf().fieldOf("biomes").forGetter(config -> config.biomes),
                        //Codec.unboundedMap(Codec.INT,
                                        //Codec.unboundedMap(Codec.BOOL, PlacedFeature.REGISTRY_CODEC))
                                //.fieldOf("features")
                                //.forGetter(config -> config.features)
                ).apply(instance, SurfaceConfig::new));
        private Integer min;
        private Integer yT;
        private Integer max;
        private String mode;
        private List<Identifier> biomes;
        private Map<Integer,Map<Boolean, RegistryEntry<PlacedFeature>>> features;

        SurfaceConfig(Integer yT,Integer min, Integer max,String mode
                      //List<Identifier> biomes,Map<Integer,Map<Boolean,RegistryEntry<PlacedFeature>>> features
        ) {
            this.yT=yT;
            this.max = max;
            this.min = min;
            this.mode=mode;
            this.biomes = biomes;
            this.features=features;
        }
    }
    private boolean simplech(boolean m,int xf,int yo,int zf){
        List<IntPair> search = Arrays.asList(
                new IntPair(xf-1,zf),
                new IntPair(xf+1,zf),
                new IntPair(xf,zf-1),
                new IntPair(xf,zf+1)
        );
        for (IntPair pos:search){
            Map<Integer,Integer>pairs=GLOBAL.get(pos);
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
    private void globalcaching(int xi,int zi){
        for (int x=-16;x<=31;x++){
            for (int z=-16;z<=31;z++) {
                IntPair key=new IntPair(xi+x,zi+z);
                TreeMap<Integer,Integer> surf=GLOBAL.get(key);
                if (surf == null) continue;
                for (Map.Entry<Integer, Integer> entry : surf.entrySet()){
                    int y=entry.getKey();
                    int i=entry.getValue();
                    boolean m=i>0;
                    if (Math.abs(i)==1){
                        if(simplech(m,xi+x,y,zi+z)){entry.setValue(m?17:-17);}
                        else {entry.setValue(m?33:-33);}
                    }
                }
                GLOBAL.put(key,surf);
            }
        }
    }
    public static Map<IntPair, TreeMap<Integer, Integer>> global(StructureWorldAccess world, int centerChunkX, int centerChunkZ) {
        Map<IntPair, TreeMap<Integer, Integer>> global = new HashMap<>();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int chunkX = centerChunkX + dx;
                int chunkZ = centerChunkZ + dz;
                ChunkPos pos = new ChunkPos(chunkX, chunkZ);
                Chunk chunk = world.getChunk(chunkX, chunkZ, ChunkStatus.EMPTY, false);

                if (!(chunk instanceof ChunkExtension ext)) continue;

                Map<IntPair, TreeMap<Integer, Integer>> local = ext.getCustomMap();
                for (Map.Entry<IntPair, TreeMap<Integer, Integer>> entry : local.entrySet()) {
                    // putIfAbsent to avoid overwriting existing
                    global.putIfAbsent(entry.getKey(), new TreeMap<>(entry.getValue()));
                }
            }
        }

        return global;
    }
    private boolean inside(IntPair XZ, int yl,boolean m) {
        TreeMap<Integer, Integer> levels = GLOBAL.get(XZ);
        if (levels == null || levels.isEmpty()) {
            return false;
        }
        Integer yf = m?levels.higherKey(yl):levels.lowerKey(yl);
        if (yf==null){return false;}
        Integer i = levels.get(yf);
        if (i==null) {return false;}
        return i>0;
    }
    private boolean wallch(int x,int z,int yl,boolean m) {
        int dy=m?3:-3;
        for(int xn=x-2;xn<=x+2;){
            for(int zn=z-2;zn<=z+2;){
                if(inside(new IntPair(xn,zn),yl+dy,m)){return true;}
                zn+=2;
            }
            xn+=2;
        }
        return false;
    }
    private void getsmooth(
            Map<BlockPos,List<BlockPos>>map,
            Map<BlockPos,List<BlockPos>>edgeS,
            Map<BlockPos,List<BlockPos>>edgeW,
            Integer x,Integer y,Integer z,
            boolean m){
        List<BlockPos> local=new ArrayList<>();
        List<IntPair> search = Arrays.asList(
                new IntPair(x-1,z),
                new IntPair(x+1,z),
                new IntPair(x,z-1),
                new IntPair(x,z+1)
        );
        for (IntPair pos:search){
            Map<Integer,Integer>pairs=GLOBAL.get(pos);
            if(pairs==null){continue;}
            for(int yl=y-1;yl<=y+1;yl++){
                Integer i=pairs.get(yl);
                if(i==null){continue;}
                if(m?i>=2&&i<=32:i<=-2&&i>=-32){local.add(new BlockPos(pos.first(),yl,pos.second()));}
            }
        }
        if (local.size()<4){
            if (wallch(x,z,y,m)){edgeW.put(new BlockPos(x,y,z),local);}
            else {edgeS.put(new BlockPos(x,y,z),local);}
        }
        if (Math.random() < 0.58095) {
            search = Arrays.asList(
                    new IntPair(x-1,z-1),
                    new IntPair(x-1,z+1),
                    new IntPair(x+1,z-1),
                    new IntPair(x+1,z+1)
            );
            for (IntPair pos:search){
                Map<Integer,Integer>pairs=GLOBAL.get(pos);
                if(pairs==null){continue;}
                Integer i=pairs.get(y);
                if(i==null){continue;}
                if(m?i>=2&&i<=32:i<=-2&&i>=-32){local.add(new BlockPos(pos.first(),y,pos.second()));}
            }
        }
        map.put(new BlockPos(x,y,z),local);
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
    void neighbours(){
        for(Map.Entry<IntPair, TreeMap<Integer, Integer>> XZ:GLOBAL.entrySet()) {
            TreeMap<Integer, Integer> sorted = new TreeMap<>(XZ.getValue());
            IntPair xz = XZ.getKey();
            Integer x = xz.first();
            Integer z = xz.second();
            Map<Integer, Integer> positions = XZ.getValue();
            for (Map.Entry<Integer, Integer> point : positions.entrySet()) {
                int y = point.getKey();
                int i = point.getValue();
                if (i>1&&i<33){
                    getsmooth(mapSF,outSF,inSF,x,y,z,true);
                }
                if (i<-1&&i>-33){
                    getsmooth(mapSC,outSC,inSC,x,y,z,false);
                }
            }
        }
        calcsmooth(mapSF,outSF,inSF,true);
        calcsmooth(mapSC,outSC,inSC,false);
    }

    private void calcsmooth(
            Map<BlockPos, List<BlockPos>> mapS,
            Map<BlockPos, List<BlockPos>> outS,
            Map<BlockPos, List<BlockPos>> inS,
            boolean m
    ) {
        Set<BlockPos> ban = new HashSet<>();
        Set<BlockPos> use = new HashSet<>(outS.keySet());
        for (int n = 15; n > 0; --n) {
            for (BlockPos point : use) {
                IntPair XZ = new IntPair(point.getX(), point.getZ());
                TreeMap<Integer, Integer> floor = GLOBAL.get(XZ);
                if (floor == null) continue;
                int yl = point.getY();
                int val = floor.get(yl);
                floor.put(yl, m ? val-n : val+n);
                GLOBAL.put(XZ, floor);
            }

            ban.addAll(use);

            Set<BlockPos> next = new HashSet<>();
            for (BlockPos pos : use) {
                List<BlockPos> neighbors = mapS.get(pos);
                if (neighbors != null) next.addAll(neighbors);
            }
            next.removeAll(ban);
            use = next;
        }
        ban = new HashSet<>();
        use = new HashSet<>(inS.keySet());
        for (int n = 15; n > 0; --n) {
            for (BlockPos point : use) {
                IntPair XZ = new IntPair(point.getX(), point.getZ());
                TreeMap<Integer, Integer> floor = GLOBAL.get(XZ);
                if (floor == null) continue;
                int yl = point.getY();
                int val = floor.get(yl);
                if (MODE=="blend"){
                    floor.put(yl, m ? val+n : val-n);
                } else{
                    if((m?17-val:val-17)<n){
                        floor.put(yl,m?17+n:-17-n);
                    }
                }

                GLOBAL.put(XZ, floor);
            }

            ban.addAll(use);

            Set<BlockPos> next = new HashSet<>();
            for (BlockPos pos : use) {
                List<BlockPos> neighbors = mapS.get(pos);
                if (neighbors != null) next.addAll(neighbors);
            }
            next.removeAll(ban);
            use = next;
        }
    }
    private List<Pair<BlockPos,Integer>>placer(StructureWorldAccess world,int xi,int zi,int yT){
        List<Pair<BlockPos,Integer>>goal=new ArrayList<>();
        ChunkPos chunkPos = world.getChunk(new BlockPos(xi,yT,zi)).getPos();
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;
        GLOBAL=global(world,chunkX,chunkZ);
        globalcaching(xi,zi);
        neighbours();
        for (int x=0;x<=15;x++){
            for (int z=0;z<=15;z++) {
                Map<Integer,Integer> ydata=GLOBAL.get(new IntPair(xi+x,zi+z));
                for (Map.Entry<Integer, Integer> entry : ydata.entrySet()){
                    goal.add(new Pair<>(new BlockPos(xi+x,entry.getKey(),zi+z), entry.getValue()));
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
            case 18:
                block=Registries.BLOCK.get(new Identifier("minecraft:brown_wool")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 19:
                block=Registries.BLOCK.get(new Identifier("minecraft:red_wool")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 20:
                block=Registries.BLOCK.get(new Identifier("minecraft:orange_wool")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 21:
                block=Registries.BLOCK.get(new Identifier("minecraft:yellow_wool")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 22:
                block=Registries.BLOCK.get(new Identifier("minecraft:lime_wool")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 23:
                block=Registries.BLOCK.get(new Identifier("minecraft:green_wool")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 24:
                block=Registries.BLOCK.get(new Identifier("minecraft:cyan_wool")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 25:
                block=Registries.BLOCK.get(new Identifier("minecraft:light_blue_wool")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 26:
                block=Registries.BLOCK.get(new Identifier("minecraft:blue_wool")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 27:
                block=Registries.BLOCK.get(new Identifier("minecraft:purple_wool")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 28:
                block=Registries.BLOCK.get(new Identifier("minecraft:magenta_wool")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 29:
                block=Registries.BLOCK.get(new Identifier("minecraft:pink_wool")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 30:
                block=Registries.BLOCK.get(new Identifier("minecraft:white_wool")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 31:
                block=Registries.BLOCK.get(new Identifier("minecraft:light_gray_wool")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
            case 32:
                block=Registries.BLOCK.get(new Identifier("minecraft:gray_wool")).getDefaultState();
                world.setBlockState(pos,block,3);
                return;
        }
    }
    @Override
    public boolean generate(FeatureContext<SurfaceConfig> context) {
        SurfaceConfig config = context.getConfig();
        MODE=config.mode;
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
            test(Math.abs(i),pos,world);
            //config.defaultFeature.value().generateUnregistered(world, chunkGenerator, random,pos);
        }
        cleancache();
        return true;
    }
    private BlockState blockB(String id){
        return Registries.BLOCK.get(new Identifier(id)).getDefaultState();
    }
    private void cleancache(){
        GLOBAL.clear();
        mapSF.clear();
        inSF.clear();
        outSF.clear();
        mapSC.clear();
        inSC.clear();
        outSC.clear();
    };
}