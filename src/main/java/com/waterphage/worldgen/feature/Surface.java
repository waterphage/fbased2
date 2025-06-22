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

    // Global objects since referencing them individually was tiresome. Not much time wasted
    private static String MODE; // Outdated
    private static Map<IntPair,TreeMap<Integer,Integer>> GLOBAL =new HashMap<>(); // Main surface data Mar(x,z)Treemap(y,i)
    private static Map<BlockPos,List<BlockPos>> mapSF=new HashMap<>(); // Smooth floor neighbours
    private static Map<BlockPos,List<BlockPos>> inSF=new HashMap<>();  // Inner smooth floor edge
    private static Map<BlockPos,List<BlockPos>> outSF=new HashMap<>(); // Outer smooth floor edge
    private static Map<BlockPos,List<BlockPos>> wallF=new HashMap<>(); // Steep floor neigbours
    private static Map<BlockPos,List<BlockPos>> mapSC=new HashMap<>(); // Smooth ceiling neighbours
    private static Map<BlockPos,List<BlockPos>> inSC=new HashMap<>();  // Inner smooth ceiling edge
    private static Map<BlockPos,List<BlockPos>> outSC=new HashMap<>(); // Outer smooth ceiling edge
    private static Map<BlockPos,List<BlockPos>> wallC=new HashMap<>(); // Steep ceiling neigbours

    // 0 Main method
    @Override
    public boolean generate(FeatureContext<SurfaceConfig> context) {
        SurfaceConfig config = context.getConfig();
        MODE=config.mode;// it's outdated I already calculated that there is ony one way to do inner/outer border. I thought that I could make smooth transition between inner and outer edge, but border need twice bigger alphabet
        BlockPos origin = context.getOrigin();
        StructureWorldAccess world = context.getWorld();
        Random random = context.getRandom();
        ChunkGenerator chunkGenerator = context.getGenerator();
        int xi=origin.getX();int zi=origin.getZ();
        BlockPos.Mutable or = new BlockPos.Mutable();
        List<Pair<BlockPos,Integer>>placer=placer(world,xi,zi,config.yT);// 1 holds all math stores placement positions and their indexes
        for (Pair<BlockPos,Integer> entry:placer){
            BlockPos.Mutable pos = entry.getLeft().mutableCopy();
            int i = entry.getRight();
            test(i,pos,world,random);// 2 Temporary filler
            //config.defaultFeature.value().generateUnregistered(world, chunkGenerator, random,pos); - feature placement reminder
        }
        cleancache();// 0.1 data cleanup
        return true;
    }
    // 0.1 data cleanup Storing data makes reading too long. Maybe there are better ways than cleaning everything every new chunk, but it just a lot of shit code that would barely save anything.
    private void cleancache(){
        GLOBAL.clear();
        mapSF.clear();
        inSF.clear();
        outSF.clear();
        mapSC.clear();
        inSC.clear();
        outSC.clear();
    };
    // 1 Placement positions calculation
    private List<Pair<BlockPos,Integer>>placer(StructureWorldAccess world,int xi,int zi,int yT){
        List<Pair<BlockPos,Integer>>goal=new ArrayList<>();
        ChunkPos chunkPos = world.getChunk(new BlockPos(xi,yT,zi)).getPos();
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;
        GLOBAL=global(world,chunkX,chunkZ); // 1.1 Surface data reading
        globalcaching(xi,zi); // 1.2 Steep / Smooth terrain division
        neighbours(); // 1.3 Edge calculation
        for (int x=0;x<=15;x++){
            for (int z=0;z<=15;z++) {
                Map<Integer,Integer> ydata=GLOBAL.get(new IntPair(xi+x,zi+z)); //reading updated surface data
                for (Map.Entry<Integer, Integer> entry : ydata.entrySet()){
                    goal.add(new Pair<>(new BlockPos(xi+x,entry.getKey(),zi+z), entry.getValue())); // converting to placement positions format
                }
            }
        }
        return goal;
    }
    // 1.1 Yes, I read data from chunk. It's dark vibecoding magic I'm too afraid to understand how it works. It was pain, alot of pain.
    public static Map<IntPair, TreeMap<Integer, Integer>> global(StructureWorldAccess world, int centerChunkX, int centerChunkZ) {
        Map<IntPair, TreeMap<Integer, Integer>> global = new HashMap<>();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int chunkX = centerChunkX + dx;
                int chunkZ = centerChunkZ + dz;
                ChunkPos pos = new ChunkPos(chunkX, chunkZ);
                Chunk chunk = world.getChunk(chunkX, chunkZ, ChunkStatus.EMPTY, false);

                if (!(chunk instanceof ChunkExtension ext)) continue;

                Map<IntPair, TreeMap<Integer, Integer>> local = ext.getCustomMap(); // I use Treemap to have strict order of y-levels. At least I hope it works this way.
                for (Map.Entry<IntPair, TreeMap<Integer, Integer>> entry : local.entrySet()) {
                    // putIfAbsent to avoid overwriting existing
                    global.putIfAbsent(entry.getKey(), new TreeMap<>(entry.getValue()));
                }
            }
        }
        return global;
    }
    // 1.2 Smooth and steep terrain determination
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
                        if(simplech(m,xi+x,y,zi+z)){entry.setValue(m?17:-17);} // 1.2.1 checking for smooth terrain 2-16 - outer edge, 17 - filling, 18-32 - inner edge
                        else {
                            if(m){wallF.put(new BlockPos(xi+x,y,zi+z),new ArrayList<>());} // pointed out wall tiles. Should probably make the same with smooth tiles,
                            else {wallC.put(new BlockPos(xi+x,y,zi+z),new ArrayList<>());}
                            entry.setValue(m?33:-33); // steep terrain code. After long thought, not the best ide to place it here since surface must go like: outer edge -> filling -> inner edge -> wall
                        }
                    }
                }
                GLOBAL.put(key,surf);
            }
        }
    }
    // 1.2.1 Smooth terrain check. This part makes + pattern. They are best to make nice transitions, tested before
    private boolean simplech(boolean m,int xf,int yo,int zf){
        List<IntPair> search = Arrays.asList( // + pattern. I doesn't matter (in local sence) if positions aren't loaded.
                new IntPair(xf-1,zf),
                new IntPair(xf+1,zf),
                new IntPair(xf,zf-1),
                new IntPair(xf,zf+1)
        );
        for (IntPair pos:search){
            Map<Integer,Integer>pairs=GLOBAL.get(pos);
            if (pairs==null){return false;}
            if(!checklocal(pairs,yo-1,yo+1,m)){return false;} // 1.2.1.1 checing for surface instead of air
        }
        return true;
    }
    //1.2.1.1 surface check
    private boolean checklocal(Map<Integer,Integer>pairs,int ymin,int ymax,boolean m){
        return pairs.entrySet().stream()
                .anyMatch(entry -> {
                    int y = entry.getKey();
                    int i = entry.getValue();
                    return y >= ymin && y <= ymax && ((m && i > 0) || (!m && i < 0));
                });
    }
    //1.3 Edge detection and calculation
    void neighbours(){
        for(Map.Entry<IntPair, TreeMap<Integer, Integer>> XZ:GLOBAL.entrySet()) { // I think this can be optimized if I pointed out all their positions earlier
            TreeMap<Integer, Integer> sorted = new TreeMap<>(XZ.getValue());
            IntPair xz = XZ.getKey();
            Integer x = xz.first();
            Integer z = xz.second();
            Map<Integer, Integer> positions = XZ.getValue();
            for (Map.Entry<Integer, Integer> point : positions.entrySet()) {
                int y = point.getKey();
                int i = point.getValue();
                if (i>1&&i<33){
                    getsmooth(mapSF,outSF,inSF,x,y,z,true); //1.3.1 calculating neighbours mesh and edges
                }
                if (i<-1&&i>-33){
                    getsmooth(mapSC,outSC,inSC,x,y,z,false);
                }
            }
        }
        calcsmooth(mapSF,outSF,inSF,true); // 1.3.2 determining smooth values
        calcsmooth(mapSC,outSC,inSC,false);
        getsteep(wallF,true); // 1.3.3 calculating steep mesh. If adjustment wall tile higher (or lover) that node - it's it neighbour
        getsteep(wallC,false);
        calcsteep(wallF,inSF,true); // 1.3.4 I made something, not entirely sure if it works. You saw why
        calcsteep(wallC,inSC,false);

    }
    // 1.3.1 Smooth mesh
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
        ); // Should probably move it out since I'm using it too often
        for (IntPair pos:search){
            Map<Integer,Integer>pairs=GLOBAL.get(pos);
            if(pairs==null){continue;}
            for(int yl=y-1;yl<=y+1;yl++){ // Same principle as simplecheck
                Integer i=pairs.get(yl);
                if(i==null){continue;} // I should check am I really that later.
                if(m?i>=2&&i<=32:i<=-2&&i>=-32){local.add(new BlockPos(pos.first(),yl,pos.second()));}
            }
        }
        if (local.size()<4){ // Pretty simple criteria
            if (wallch(x,z,y,m)){ // 1.3.1.1 Checking for wall to find inner edge
                if(m){wallF.put(new BlockPos(x,y,z),new ArrayList<>());} // It easier to impregnate to wall mesh that dancing around maps
                else {wallC.put(new BlockPos(x,y,z),new ArrayList<>());}
                edgeW.put(new BlockPos(x,y,z),local);} // inner edge
            else {edgeS.put(new BlockPos(x,y,z),local);} // outer edge
        }
        if (Math.random() < 0.58095) { // making X neighbours to make edge round
            search = Arrays.asList( // I'm using this only here
                    new IntPair(x-1,z-1),
                    new IntPair(x-1,z+1),
                    new IntPair(x+1,z-1),
                    new IntPair(x+1,z+1)
            );
            for (IntPair pos:search){ //diamond pattern cooler on slopes, and it's already eating my time, so only one y-level
                Map<Integer,Integer>pairs=GLOBAL.get(pos);
                if(pairs==null){continue;}
                Integer i=pairs.get(y);
                if(i==null){continue;}
                if(m?i>=2&&i<=32:i<=-2&&i>=-32){local.add(new BlockPos(pos.first(),y,pos.second()));}
            }
        }
        map.put(new BlockPos(x,y,z),local);
    }
    // 1.3.1.1
    private boolean wallch(int x,int z,int yl,boolean m) { // checks for wall
        int dy=0; // I tried many combinations of positions at y and xz. All of them sucks
        List<IntPair> search = Arrays.asList(
                new IntPair(x-1,z),
                new IntPair(x+1,z),
                new IntPair(x,z-1),
                new IntPair(x,z+1)
        );
        for(IntPair point:search){
            if(inside(point,yl+dy,m)){return true;} // 1.3.1.1.1 looking for steep floor
        }
        return false;
    }
    // 1.3.1.1.1 looking for steep floor
    private boolean inside(IntPair XZ, int yl,boolean m) {
        TreeMap<Integer, Integer> levels = GLOBAL.get(XZ);
        if (levels == null || levels.isEmpty()) {
            return false;
        }
        Integer yf = m?levels.higherKey(yl):levels.lowerKey(yl);
        if (yf==null){return false;}
        Integer i = levels.get(yf);
        if (i==null) {return false;}
        return m?i>32:i<-32;
    }
    // 1.3.2 determining i-values for smooth mesh
    private void calcsmooth(
            Map<BlockPos, List<BlockPos>> mapS,
            Map<BlockPos, List<BlockPos>> outS,
            Map<BlockPos, List<BlockPos>> inS,
            boolean m
    ) {
        Set<BlockPos> ban = new HashSet<>();
        Set<BlockPos> use = new HashSet<>(outS.keySet());
        for (int n = 15; n > 0; --n) {// inner edge
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
        for (int n = 15; n > 0; --n) { // outer edge
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
    // 1.3.3 Calculatind sttep mesh
    private void getsteep(Map<BlockPos,List<BlockPos>> wall,boolean m){
        Map<BlockPos, List<BlockPos>> updated = new HashMap<>();
        for (BlockPos point:wall.keySet()){
            int x=point.getX();int z= point.getZ();int y=point.getY();
            List<IntPair> search = Arrays.asList(
                    new IntPair(x-1,z),
                    new IntPair(x+1,z),
                    new IntPair(x,z-1),
                    new IntPair(x,z+1)
            );
            List<BlockPos> neighbours=new ArrayList<>();
            for (IntPair pos:search){
                TreeMap<Integer,Integer>pairs=GLOBAL.get(pos);
                if (pairs==null) continue;
                Integer yw=m?pairs.higherKey(y):pairs.lowerKey(y);
                if(yw==null) continue;
                Integer iw=pairs.get(yw);
                if(iw==null) continue;
                if(m?pairs.get(yw)>32:pairs.get(yw)<-32){
                    neighbours.add(new BlockPos(pos.first(),yw,pos.second()));
                }
            }
            updated.put(point, neighbours);
        }
        wall.clear();
        wall.putAll(updated);
    }
    // 1.3.4 Steep calcutaions. Not sure if it's workking as intended since i lost track.

    private void calcsteep(Map<BlockPos,List<BlockPos>> wall,Map<BlockPos,List<BlockPos>> edgei,boolean m){
        Map<BlockPos,List<BlockPos>> edge=new HashMap<>(edgei);
        List<BlockPos> ban=new ArrayList<>();
        for (int n=16;n>0;--n){//push calculation further
            if (edge.isEmpty())return;
            ban.addAll(edge.keySet());
            Map<BlockPos,List<BlockPos>> upd=new HashMap<>();
            for (BlockPos start:edge.keySet()){// main cycle
                Map<Integer,Integer>ydata=GLOBAL.get(new IntPair(start.getX(),start.getZ()));
                if (ydata==null)continue;
                int ys=start.getY();
                Integer isi=Math.abs(ydata.get(ys));
                if (isi==null)continue;
                Integer is=isi<33?16:isi-33;// I decided formulas on a go. It only looks good. To tired to fully calculate this crap.

                for(BlockPos neig:wall.get(start)){
                    if(neig==null)continue;
                    int yn=neig.getY();
                    int dy=m?neig.getY()-ys:ys-neig.getY();
                    IntPair XZn=new IntPair(neig.getX(),neig.getZ());
                    Integer it=GLOBAL.get(XZn).get(yn);
                    if (it==null)continue;
                    Integer i=Math.abs(it)-33;

                    if (is-dy-1>i){
                        TreeMap <Integer,Integer>ydata2=GLOBAL.get(XZn);
                        int ifin=(m?1:-1)*(is-dy-1+33);

                        ydata2.put(yn,ifin);
                        GLOBAL.put(XZn,ydata2);
                        upd.put(neig,wall.get(neig));
                    }
                }
            }
            edge.putAll(upd);
            for (BlockPos b:ban){
                edge.remove(b);
            }
        }
    }

    // 2 Cool glacier thing for testing.
    private void test(int i,BlockPos.Mutable pos,StructureWorldAccess world,Random random){
        BlockState snow=Registries.BLOCK.get(new Identifier("minecraft:snow_block")).getDefaultState();
        BlockState ice=Registries.BLOCK.get(new Identifier("minecraft:packed_ice")).getDefaultState();
        BlockState dirt=Registries.BLOCK.get(new Identifier("minecraft:coarse_dirt")).getDefaultState();
        BlockState surf=snow;
        switch (i){
            case 2:
                if (random.nextInt(6)<5) {surf=dirt;}
                world.setBlockState(pos,surf,3);
                return;
            case 3:
                if (random.nextInt(6)<4) {surf=dirt;}
                world.setBlockState(pos,surf,3);
                return;
            case 4:
                if (random.nextInt(6)<3) {surf=dirt;}
                world.setBlockState(pos,surf,3);
                return;
            case 5:
                if (random.nextInt(6)<2) {surf=dirt;}
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),surf,3);
                return;
            case 6:
                if (random.nextInt(6)<1) {surf=dirt;}
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),surf,3);
                return;
            case 7:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),snow,3);
                return;
            case 8:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                return;
            case 9:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                return;
            case 10:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),ice,3);
                world.setBlockState(pos.add(0,10,0),ice,3);
                return;
            case 11:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),ice,3);
                world.setBlockState(pos.add(0,10,0),ice,3);
                world.setBlockState(pos.add(0,11,0),ice,3);
                world.setBlockState(pos.add(0,12,0),ice,3);
                return;
            case 12:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),ice,3);
                world.setBlockState(pos.add(0,10,0),ice,3);
                world.setBlockState(pos.add(0,11,0),ice,3);
                world.setBlockState(pos.add(0,12,0),snow,3);
                world.setBlockState(pos.add(0,13,0),snow,3);
                return;
            case 13:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),ice,3);
                world.setBlockState(pos.add(0,10,0),ice,3);
                world.setBlockState(pos.add(0,11,0),snow,3);
                world.setBlockState(pos.add(0,12,0),snow,3);
                world.setBlockState(pos.add(0,13,0),snow,3);
                world.setBlockState(pos.add(0,14,0),snow,3);
                return;
            case 14:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),ice,3);
                world.setBlockState(pos.add(0,10,0),snow,3);
                world.setBlockState(pos.add(0,11,0),snow,3);
                world.setBlockState(pos.add(0,12,0),snow,3);
                world.setBlockState(pos.add(0,13,0),snow,3);
                world.setBlockState(pos.add(0,14,0),snow,3);
                return;
            case 15:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),snow,3);
                world.setBlockState(pos.add(0,10,0),snow,3);
                world.setBlockState(pos.add(0,11,0),snow,3);
                world.setBlockState(pos.add(0,12,0),snow,3);
                world.setBlockState(pos.add(0,13,0),snow,3);
                world.setBlockState(pos.add(0,14,0),snow,3);
                world.setBlockState(pos.add(0,15,0),snow,3);
                return;
            case 16:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),snow,3);
                world.setBlockState(pos.add(0,9,0),snow,3);
                world.setBlockState(pos.add(0,10,0),snow,3);
                world.setBlockState(pos.add(0,11,0),snow,3);
                world.setBlockState(pos.add(0,12,0),snow,3);
                world.setBlockState(pos.add(0,13,0),snow,3);
                world.setBlockState(pos.add(0,14,0),snow,3);
                world.setBlockState(pos.add(0,15,0),snow,3);
                return;
            case 17:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),snow,3);
                world.setBlockState(pos.add(0,8,0),snow,3);
                world.setBlockState(pos.add(0,9,0),snow,3);
                world.setBlockState(pos.add(0,10,0),snow,3);
                world.setBlockState(pos.add(0,11,0),snow,3);
                world.setBlockState(pos.add(0,12,0),snow,3);
                world.setBlockState(pos.add(0,13,0),snow,3);
                world.setBlockState(pos.add(0,14,0),snow,3);
                world.setBlockState(pos.add(0,15,0),snow,3);
                return;
            case 18:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),snow,3);
                world.setBlockState(pos.add(0,8,0),snow,3);
                world.setBlockState(pos.add(0,9,0),snow,3);
                world.setBlockState(pos.add(0,10,0),snow,3);
                world.setBlockState(pos.add(0,11,0),snow,3);
                world.setBlockState(pos.add(0,12,0),snow,3);
                world.setBlockState(pos.add(0,13,0),snow,3);
                world.setBlockState(pos.add(0,14,0),snow,3);
                return;
            case 19:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),snow,3);
                world.setBlockState(pos.add(0,9,0),snow,3);
                world.setBlockState(pos.add(0,10,0),snow,3);
                world.setBlockState(pos.add(0,11,0),snow,3);
                world.setBlockState(pos.add(0,12,0),snow,3);
                world.setBlockState(pos.add(0,13,0),snow,3);
                return;
            case 20:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),snow,3);
                world.setBlockState(pos.add(0,9,0),snow,3);
                world.setBlockState(pos.add(0,10,0),snow,3);
                world.setBlockState(pos.add(0,11,0),snow,3);
                world.setBlockState(pos.add(0,12,0),snow,3);
                world.setBlockState(pos.add(0,13,0),snow,3);
                world.setBlockState(pos.add(0,14,0),snow,3);
                return;
            case 21:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),dirt,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),snow,3);
                world.setBlockState(pos.add(0,10,0),snow,3);
                world.setBlockState(pos.add(0,11,0),snow,3);
                world.setBlockState(pos.add(0,12,0),snow,3);
                world.setBlockState(pos.add(0,13,0),snow,3);
                world.setBlockState(pos.add(0,14,0),snow,3);
                world.setBlockState(pos.add(0,15,0),snow,3);
                return;
            case 22:
                world.setBlockState(pos,dirt,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),snow,3);
                world.setBlockState(pos.add(0,10,0),snow,3);
                world.setBlockState(pos.add(0,11,0),snow,3);
                world.setBlockState(pos.add(0,12,0),snow,3);
                world.setBlockState(pos.add(0,13,0),snow,3);
                world.setBlockState(pos.add(0,14,0),snow,3);
                world.setBlockState(pos.add(0,15,0),snow,3);
                return;
            case 23:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),ice,3);
                world.setBlockState(pos.add(0,10,0),snow,3);
                world.setBlockState(pos.add(0,11,0),snow,3);
                world.setBlockState(pos.add(0,12,0),snow,3);
                world.setBlockState(pos.add(0,13,0),snow,3);
                world.setBlockState(pos.add(0,14,0),snow,3);
                world.setBlockState(pos.add(0,15,0),dirt,3);
                return;
            case 24:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),ice,3);
                world.setBlockState(pos.add(0,10,0),snow,3);
                world.setBlockState(pos.add(0,11,0),snow,3);
                world.setBlockState(pos.add(0,12,0),snow,3);
                world.setBlockState(pos.add(0,13,0),snow,3);
                world.setBlockState(pos.add(0,14,0),dirt,3);
                world.setBlockState(pos.add(0,15,0),dirt,3);
                return;
            case 25:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),ice,3);
                world.setBlockState(pos.add(0,10,0),ice,3);
                world.setBlockState(pos.add(0,11,0),snow,3);
                world.setBlockState(pos.add(0,12,0),snow,3);
                world.setBlockState(pos.add(0,13,0),snow,3);
                world.setBlockState(pos.add(0,14,0),snow,3);
                world.setBlockState(pos.add(0,15,0),dirt,3);
                return;
            case 26:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),ice,3);
                world.setBlockState(pos.add(0,10,0),ice,3);
                world.setBlockState(pos.add(0,11,0),snow,3);
                world.setBlockState(pos.add(0,12,0),snow,3);
                world.setBlockState(pos.add(0,13,0),snow,3);
                world.setBlockState(pos.add(0,14,0),snow,3);
                world.setBlockState(pos.add(0,15,0),snow,3);
                return;
            case 27:
                world.setBlockState(pos,dirt,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),ice,3);
                world.setBlockState(pos.add(0,10,0),ice,3);
                world.setBlockState(pos.add(0,11,0),ice,3);
                world.setBlockState(pos.add(0,12,0),snow,3);
                world.setBlockState(pos.add(0,13,0),snow,3);
                world.setBlockState(pos.add(0,14,0),snow,3);
                world.setBlockState(pos.add(0,15,0),snow,3);
                return;
            case 28:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),ice,3);
                world.setBlockState(pos.add(0,10,0),ice,3);
                world.setBlockState(pos.add(0,11,0),ice,3);
                world.setBlockState(pos.add(0,12,0),snow,3);
                world.setBlockState(pos.add(0,13,0),snow,3);
                world.setBlockState(pos.add(0,14,0),snow,3);
                world.setBlockState(pos.add(0,15,0),snow,3);
                return;
            case 29:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),ice,3);
                world.setBlockState(pos.add(0,10,0),ice,3);
                world.setBlockState(pos.add(0,11,0),ice,3);
                world.setBlockState(pos.add(0,12,0),ice,3);
                world.setBlockState(pos.add(0,13,0),snow,3);
                world.setBlockState(pos.add(0,14,0),snow,3);
                world.setBlockState(pos.add(0,15,0),dirt,3);
                return;
            case 30:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),ice,3);
                world.setBlockState(pos.add(0,10,0),ice,3);
                world.setBlockState(pos.add(0,11,0),ice,3);
                world.setBlockState(pos.add(0,12,0),ice,3);
                world.setBlockState(pos.add(0,13,0),snow,3);
                world.setBlockState(pos.add(0,14,0),dirt,3);
                world.setBlockState(pos.add(0,15,0),dirt,3);
                return;
            case 31:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),ice,3);
                world.setBlockState(pos.add(0,10,0),ice,3);
                world.setBlockState(pos.add(0,11,0),ice,3);
                world.setBlockState(pos.add(0,12,0),ice,3);
                world.setBlockState(pos.add(0,13,0),ice,3);
                world.setBlockState(pos.add(0,14,0),dirt,3);
                world.setBlockState(pos.add(0,15,0),dirt,3);
                return;
            case 32:
                world.setBlockState(pos,ice,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),ice,3);
                world.setBlockState(pos.add(0,10,0),ice,3);
                world.setBlockState(pos.add(0,11,0),ice,3);
                world.setBlockState(pos.add(0,12,0),ice,3);
                world.setBlockState(pos.add(0,13,0),ice,3);
                world.setBlockState(pos.add(0,14,0),ice,3);
                world.setBlockState(pos.add(0,15,0),dirt,3);
                return;
            case 48:
                world.setBlockState(pos,dirt,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),ice,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),ice,3);
                world.setBlockState(pos.add(0,10,0),ice,3);
                world.setBlockState(pos.add(0,11,0),ice,3);
                world.setBlockState(pos.add(0,12,0),ice,3);
                world.setBlockState(pos.add(0,13,0),ice,3);
                world.setBlockState(pos.add(0,14,0),snow,3);
                return;
            case 47:
                world.setBlockState(pos,dirt,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),dirt,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),ice,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),ice,3);
                world.setBlockState(pos.add(0,10,0),ice,3);
                world.setBlockState(pos.add(0,11,0),ice,3);
                world.setBlockState(pos.add(0,12,0),ice,3);
                world.setBlockState(pos.add(0,13,0),snow,3);
                return;
            case 46:
                world.setBlockState(pos,dirt,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),dirt,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),dirt,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),ice,3);
                world.setBlockState(pos.add(0,10,0),ice,3);
                world.setBlockState(pos.add(0,11,0),ice,3);
                world.setBlockState(pos.add(0,12,0),snow,3);
                return;
            case 45:
                world.setBlockState(pos,dirt,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),dirt,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),dirt,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),dirt,3);
                world.setBlockState(pos.add(0,10,0),ice,3);
                world.setBlockState(pos.add(0,11,0),snow,3);
                return;
            case 44:
                world.setBlockState(pos,dirt,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),dirt,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),dirt,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),ice,3);
                world.setBlockState(pos.add(0,9,0),ice,3);
                world.setBlockState(pos.add(0,10,0),dirt,3);
                return;
            case 43:
                world.setBlockState(pos,dirt,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),dirt,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),dirt,3);
                world.setBlockState(pos.add(0,7,0),ice,3);
                world.setBlockState(pos.add(0,8,0),dirt,3);
                world.setBlockState(pos.add(0,9,0),dirt,3);
                return;
            case 42:
                world.setBlockState(pos,dirt,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),dirt,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),dirt,3);
                world.setBlockState(pos.add(0,7,0),dirt,3);
                world.setBlockState(pos.add(0,8,0),dirt,3);
                return;
            case 41:
                world.setBlockState(pos,dirt,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),dirt,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),dirt,3);
                world.setBlockState(pos.add(0,7,0),dirt,3);
                return;
            case 40:
                world.setBlockState(pos,dirt,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),dirt,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),ice,3);
                world.setBlockState(pos.add(0,6,0),dirt,3);
                return;
            case 39:
                world.setBlockState(pos,dirt,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),dirt,3);
                world.setBlockState(pos.add(0,4,0),ice,3);
                world.setBlockState(pos.add(0,5,0),snow,3);
                return;
            case 38:
                world.setBlockState(pos,dirt,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),dirt,3);
                world.setBlockState(pos.add(0,4,0),snow,3);
                return;
            case 37:
                world.setBlockState(pos,dirt,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),ice,3);
                world.setBlockState(pos.add(0,3,0),snow,3);
                return;
            case 36:
                world.setBlockState(pos,dirt,3);
                world.setBlockState(pos.add(0,1,0),ice,3);
                world.setBlockState(pos.add(0,2,0),snow,3);
                return;
            case 35:
                world.setBlockState(pos,dirt,3);
                world.setBlockState(pos.add(0,1,0),snow,3);
                return;
            case 34:
                world.setBlockState(pos,dirt,3);
                return;
        }
    }
}