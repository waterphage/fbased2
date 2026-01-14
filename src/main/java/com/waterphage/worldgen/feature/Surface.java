package com.waterphage.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import com.waterphage.meta.ChunkExtension;
import com.waterphage.meta.IntPair;
import com.waterphage.worldgen.placers.Offset;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
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
import java.util.stream.Stream;

public class Surface extends Feature<Surface.SurfaceConfig> {
    public Surface(Codec<SurfaceConfig> codec) {
        super(codec);
    }
    public record Wall(int i,RegistryEntry<PlacedFeature> feature){}
    public static final Codec<Wall> FB_WALL_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("i").forGetter(Wall::i),
                    PlacedFeature.REGISTRY_CODEC.fieldOf("f").forGetter(Wall::feature)
            ).apply(instance, Wall::new)
    );
    public record BiomeValue(int out, int in, float chance, float chance2) {}
    public static final Codec<BiomeValue> FB_BIOME_VALUE_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("out_p").forGetter(BiomeValue::out),
                    Codec.INT.fieldOf("in_p").forGetter(BiomeValue::in),
                    Codec.FLOAT.fieldOf("out_c").forGetter(BiomeValue::chance),
                    Codec.FLOAT.fieldOf("in_c").forGetter(BiomeValue::chance2)
            ).apply(instance, BiomeValue::new)
    );
    private boolean l=true;//log
    public static final UnboundedMapCodec<Integer, RegistryEntry<PlacedFeature>> FB_INDEX_FEATURE_CODEC = Codec.unboundedMap(Codec.STRING.xmap(Integer::parseInt, Object::toString), PlacedFeature.REGISTRY_CODEC);
    public static final UnboundedMapCodec<String, Map<Integer, RegistryEntry<PlacedFeature>>> FB_BIOME_FEATURE_CODEC = Codec.unboundedMap(Codec.STRING, FB_INDEX_FEATURE_CODEC);

    public static final Codec<Map<String, BiomeValue>> FB_BIOME_MAP_CODEC = Codec.unboundedMap(Codec.STRING, FB_BIOME_VALUE_CODEC);
    public static class SurfaceConfig implements FeatureConfig {
        public static final Codec<SurfaceConfig> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Codec.INT.fieldOf("tech_Y").forGetter(config -> config.yT),
                        Codec.INT.fieldOf("min").forGetter(config -> config.min),
                        Codec.INT.fieldOf("max").forGetter(config -> config.max),
                        Codec.INT.listOf().fieldOf("matrix").forGetter(config -> config.matrix),
                        FB_WALL_CODEC.listOf().fieldOf("wall").forGetter(config -> config.wall),
                        FB_BIOME_MAP_CODEC.fieldOf("biome_relations").forGetter(config -> config.biome),
                        FB_BIOME_FEATURE_CODEC.fieldOf("biome_features").forGetter(config -> config.feature)
                ).apply(instance, SurfaceConfig::new));
        private Integer min;
        private Integer yT;
        private Integer max;
        private List<Integer> matrix;
        private List<Wall> wall;
        private Map<String,BiomeValue> biome;
        private Map<String,Map<Integer,RegistryEntry<PlacedFeature>>> feature;

        SurfaceConfig(Integer yT,Integer min, Integer max,List<Integer> matrix,List<Wall> wall,Map<String,BiomeValue>biome,
                      Map<String,Map<Integer,RegistryEntry<PlacedFeature>>> feature
        ) {
            this.yT=yT;
            this.max = max;
            this.min = min;
            this.matrix=matrix;
            this.wall=wall;
            this.biome = biome;
            this.feature=feature;
        }

        public Map<String, BiomeValue> biome() {
            return this.biome;
        }
    }
    private class SurfCont {
        Chunk chunk;

        private StructureWorldAccess w;
        private Random r;
        private Map<String,BiomeValue> biomemap;
        private Wall wall;private Double bE;
        private Map<IntPair,TreeMap<Integer,Integer>> global = new HashMap<>();
        private Map<BlockPos,List<BlockPos>> mapSF = new HashMap<>();
        private Map<BlockPos,List<BlockPos>> inSF = new HashMap<>();
        private Map<BlockPos,List<BlockPos>> outSF = new HashMap<>();
        private Map<BlockPos,List<BlockPos>> wallF = new HashMap<>();
        private Map<BlockPos,List<BlockPos>> mapSC = new HashMap<>();
        private Map<BlockPos,List<BlockPos>> inSC = new HashMap<>();
        private Map<BlockPos,List<BlockPos>> outSC = new HashMap<>();
        private Map<BlockPos,List<BlockPos>> wallC = new HashMap<>();
        private Map<Integer,List<BlockPos>> biomeO = new HashMap<>();
        private Map<Integer,List<BlockPos>> biomeI = new HashMap<>();
        private Map<String,Map<Integer,RegistryEntry<PlacedFeature>>> Ftypes;
        private int xi;
        private int zi;
        private int yT;

        private float random(BlockPos pos){
            long seed = pos.asLong() ^ w.getSeed() ^ ((pos.getX()*31L) + (pos.getZ()*17L));
            java.util.Random random = new java.util.Random(seed);
            return random.nextFloat();
        }
        private SurfCont(FeatureContext<SurfaceConfig> ctx) {
            this.w = ctx.getWorld();
            this.r = ctx.getRandom();
            SurfaceConfig config=ctx.getConfig();
            this.biomemap = config.biome();
            this.Ftypes=config.feature;
            BlockPos.Mutable origin=ctx.getOrigin().mutableCopy();
            this.xi=origin.getX();this.zi=origin.getZ();
            this.yT=config.yT;
            this.chunk=this.w.getChunk(ctx.getOrigin());
            if(this.chunk instanceof ChunkExtension ext){
                List<Double>val=ext.getNoise();
                Double bX=(0.25D*val.get(0))+0.5D;// cont.json
                Double bZ=(0.25D*val.get(1))+0.5D;// eros.json
                this.bE=(0.25D*val.get(2))+0.5D;
                Integer index=Math.toIntExact(Math.round(bX*(config.matrix.get(0)-1)))+config.matrix.get(0)*Math.toIntExact(Math.round(bZ*(config.matrix.get(1)-1)));
                if (l) {System.out.println("bX=" + bX + ", bZ=" + bZ+", i="+index);l=false;}
                if (index >= 0 && index <= config.wall.size())this.wall=config.wall.get(index);
            }
        }
    }
    // 0 Main method
    @Override
    public boolean generate(FeatureContext<SurfaceConfig> context) {
        SurfCont ctx=new SurfCont(context);
        List<Pair<BlockPos,Integer>>placer=placer(ctx);// 1 holds all math stores placement positions and their indexes
        geow(ctx);
        for (Pair<BlockPos,Integer> entry:placer){
            BlockPos.Mutable pos = entry.getLeft().mutableCopy();
            int i = entry.getRight();
            test(i,pos,ctx);// 2 Temporary filler
            //config.defaultFeature.value().generateUnregistered(world, chunkGenerator, random,pos); - feature placement reminder
        }
        return true;
    }
    // 1 Placement positions calculation
    private List<Pair<BlockPos,Integer>>placer(SurfCont ctx){
        List<Pair<BlockPos,Integer>>goal=new ArrayList<>();
        ChunkPos chunkPos = ctx.w.getChunk(new BlockPos(ctx.xi,ctx.yT,ctx.zi)).getPos();
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;
        ctx.global=global(chunkX,chunkZ,ctx); // 1.1 Surface data reading
        globalcaching(ctx); // 1.2 Steep / Smooth terrain division
        neighbours(ctx); // 1.3 Edge calculation
        for (int x=0;x<=15;x++){
            for (int z=0;z<=15;z++) {
                Map<Integer,Integer> ydata=ctx.global.get(new IntPair(ctx.xi+x,ctx.zi+z)); //reading updated surface data
                for (Map.Entry<Integer, Integer> entry : ydata.entrySet()){
                    goal.add(new Pair<>(new BlockPos(ctx.xi+x,entry.getKey(),ctx.zi+z), entry.getValue())); // converting to placement positions format
                }
            }
        }
        return goal;
    }
    // 1.1 Yes, I read data from chunk. It's dark vibecoding magic I'm too afraid to understand how it works. It was pain, alot of pain.
    public static Map<IntPair, TreeMap<Integer, Integer>> global(int centerChunkX, int centerChunkZ,SurfCont ctx) {
        Map<IntPair, TreeMap<Integer, Integer>> global = new HashMap<>();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int chunkX = centerChunkX + dx;
                int chunkZ = centerChunkZ + dz;
                ChunkPos pos = new ChunkPos(chunkX, chunkZ);
                Chunk chunk = ctx.w.getChunk(chunkX, chunkZ, ChunkStatus.EMPTY, false);

                if (!(chunk instanceof ChunkExtension ext)) continue;

                Map<IntPair, TreeMap<Integer, Integer>> local = ext.getCustomMap(); // I use Treemap to have strict order of y-levels. At least I hope it works this way.
                for (Map.Entry<IntPair, TreeMap<Integer, Integer>> entry : local.entrySet()) {
                    global.put(entry.getKey(), new TreeMap<>(entry.getValue()));
                }
            }
        }
        return global;
    }
    // 1.2 Smooth and steep terrain determination
    private void globalcaching(SurfCont ctx){
        for (int x=-16;x<=31;x++){
            for (int z=-16;z<=31;z++) {
                int xf=ctx.xi+x;int zf=ctx.zi+z;
                IntPair key=new IntPair(xf,zf);
                TreeMap<Integer,Integer> surf=ctx.global.get(key);
                if (surf == null) continue;
                for (Map.Entry<Integer, Integer> entry : surf.entrySet()){
                    int y=entry.getKey();
                    int i=entry.getValue();
                    boolean m=i>0;
                    if (Math.abs(i)==1){
                        if(simplech(m,xf,y,zf,ctx)){entry.setValue(m?17:-17);} // 1.2.1 checking for smooth terrain 2-16 - outer edge, 17 - filling, 18-32 - inner edge
                        else {
                            if(m){ctx.wallF.put(new BlockPos(xf,y,zf),new ArrayList<>());} // pointed out wall tiles. Should probably make the same with smooth tiles,
                            else {ctx.wallC.put(new BlockPos(xf,y,zf),new ArrayList<>());}
                            entry.setValue(m?33:-33); // steep terrain code. After long thought, not the best ide to place it here since surface must go like: outer edge -> filling -> inner edge -> wall
                        }
                    }
                }
            }
        }
    }
    private static List<IntPair> search (int x,int z){
        return Arrays.asList(
                new IntPair(x-1,z),
                new IntPair(x+1,z),
                new IntPair(x,z-1),
                new IntPair(x,z+1)
        );
    }
    // 1.2.1 Smooth terrain check. This part makes + pattern. They are best to make nice transitions, tested before
    private boolean simplech(boolean m,int xf,int yo,int zf,SurfCont ctx){
        List<IntPair> search = search(xf,zf);
        for (IntPair pos:search){
            TreeMap<Integer,Integer>pairs=ctx.global.get(pos);
            if (pairs==null){return false;}
            if(!checklocal(pairs,yo-1,yo+1,m)){return false;} // 1.2.1.1 checking for surface instead of air
        }
        return true;
    }
    //1.2.1.1 surface check
    private boolean checklocal(TreeMap<Integer,Integer>pairs,int ymin,int ymax,boolean m){
        return pairs.subMap(ymin, true, ymax, true).values().stream()
                .anyMatch(i -> (m && i > 0) || (!m && i < 0));
    }
    //1.3 Edge detection and calculation
    void neighbours(SurfCont ctx){
        for(Map.Entry<IntPair, TreeMap<Integer, Integer>> XZ:ctx.global.entrySet()) { // I think this can be optimized if I pointed out all their positions earlier
            IntPair xz = XZ.getKey();
            Integer x = xz.first();
            Integer z = xz.second();
            Map<Integer, Integer> positions = XZ.getValue();
            for (Map.Entry<Integer, Integer> point : positions.entrySet()) {
                int y = point.getKey();
                int i = point.getValue();
                if (i>1&&i<33){
                    getsmooth(ctx,ctx.mapSF,ctx.outSF,ctx.inSF,ctx.wallF,x,y,z,true); //1.3.1 calculating neighbours mesh and edges
                }
                if (i<-1&&i>-33){
                    getsmooth(ctx,ctx.mapSC,ctx.outSC,ctx.inSC,ctx.wallC,x,y,z,false);
                }
            }
        }
        calcsmooth(ctx,ctx.mapSF,ctx.outSF,ctx.inSF,true); // 1.3.2 determining smooth values
        calcsmooth(ctx,ctx.mapSC,ctx.outSC,ctx.inSC,false);
        getsteep(ctx,ctx.wallF,true); // 1.3.3 calculating steep mesh. If adjustment wall tile higher (or lover) that node - it's it neighbour
        getsteep(ctx,ctx.wallC,false);
        calcsteep(ctx,ctx.wallF,ctx.inSF,true); // 1.3.4 I made something, not entirely sure if it works. You saw why
        calcsteep(ctx,ctx.wallC,ctx.inSC,false);

    }
    // 1.3.1 Smooth mesh
    private void getsmooth(
            SurfCont ctx,
            Map<BlockPos,List<BlockPos>>map,
            Map<BlockPos,List<BlockPos>>edgeS,
            Map<BlockPos,List<BlockPos>>edgeW,
            Map<BlockPos,List<BlockPos>>wall,
            Integer x,Integer y,Integer z,
            boolean m){
        List<BlockPos> local=new ArrayList<>();
        List<IntPair> search = search(x,z);
        for (IntPair pos:search){
            TreeMap<Integer,Integer>pairs=ctx.global.get(pos);
            if(pairs==null){continue;}
            for (Map.Entry<Integer, Integer> entry : pairs.subMap(y - 1, true, y + 1, true).entrySet()) {
                int yl = entry.getKey();
                int i = entry.getValue();
                if (m ? i >= 2 && i <= 32 : i <= -2 && i >= -32) {
                    local.add(new BlockPos(pos.first(), yl, pos.second()));
                }
            }
        }
        BlockPos org=new BlockPos(x,y,z);
        biomeBorder(ctx,org,local);
        if (local.size()<4){ // Pretty simple criteria
            wall.put(org,new ArrayList<>()); // It easier to impregnate to wall mesh that dancing around maps
            if(wallch2(ctx,x,z,y,m)){
                edgeS.put(org,local); // outer edge
            }
            if (wallch(ctx,x,z,y,m)){ // 1.3.1.1 Checking for wall to find inner edge
                edgeW.put(org,local);} // inner edge
        }
        if(local.size()<2){edgeS.put(org,local);}
        if (ctx.random(org) < 0.58095) { // making X neighbours to make edge round
            search = Arrays.asList( // I'm using this only here
                    new IntPair(x-1,z-1),
                    new IntPair(x-1,z+1),
                    new IntPair(x+1,z-1),
                    new IntPair(x+1,z+1)
            );
            for (IntPair pos:search){ //diamond pattern cooler on slopes, and it's already eating my time, so only one y-level
                Map<Integer,Integer>pairs=ctx.global.get(pos);
                if(pairs==null){continue;}
                Integer i=pairs.get(y);
                if(i==null){continue;}
                if(m?i>=2&&i<=32:i<=-2&&i>=-32){local.add(new BlockPos(pos.first(),y,pos.second()));}
            }
        }

        map.put(org,local);
    }
    private void biomeBorder(SurfCont ctx,BlockPos org,List<BlockPos>local){
        String orgB=ctx.w.getRegistryManager().get(RegistryKeys.BIOME).getId(ctx.w.getBiome(org).value()).toString();
        if(lightcheck(ctx,org,local,orgB))return;
        for (BlockPos neig:local){
            String neigB=ctx.w.getRegistryManager().get(RegistryKeys.BIOME).getId(ctx.w.getBiome(neig).value()).toString();
            String id=orgB+","+neigB;
            BiomeValue biom=ctx.biomemap.get(id);if(biom==null)continue;float rand=ctx.random(org);
            if(rand<1.0/biom.chance){writeB(org,ctx.biomeO,Math.round(biom.out*rand*biom.chance));}
            if(rand<1.0/biom.chance2){writeB(org,ctx.biomeI,Math.round(biom.in*rand*biom.chance2));}
        }
    }
    private boolean lightcheck(SurfCont ctx,BlockPos org,List<BlockPos>local,String orgB){
        int xo=org.getX();int zo=org.getZ();int yo=org.getY();
        TreeMap<Integer,Integer>ypair=ctx.global.get(new IntPair(xo,zo));
        if(ypair==null)return false;Integer ym=ypair.higherKey(yo);if(ym==null)return false;
        for (BlockPos neig:local){
            int xn=neig.getX();int zn=neig.getZ();int yn=neig.getY();
            ypair=ctx.global.get(new IntPair(xn,zn));if(ypair==null)continue;
            ym=ypair.higherKey(yn);if(ym!=null)continue;
            String key=orgB+",shadow";
            BiomeValue biom=ctx.biomemap.get(key);
            if(biom==null)continue;float rand=ctx.random(org);
            if(rand<1.0/biom.chance){writeB(org,ctx.biomeO,Math.round(biom.out*rand*biom.chance));}
            if(rand<1.0/biom.chance2){writeB(org,ctx.biomeI,Math.round(biom.in*rand*biom.chance2));}
            return true;
        }
        return false;
    }
    private void writeB(BlockPos pos, Map<Integer, List<BlockPos>> biome, int i) {
        biome.computeIfAbsent(i, k -> new ArrayList<>()).add(pos);
    }
    // 1.3.1.1
    private boolean wallch(SurfCont ctx,int x,int z,int yl,boolean m) { // checks for wall
        int dy=m?-1:1; // I tried many combinations of positions at y and xz. All of them sucks
        List<IntPair> search = search(x,z);
        for(IntPair point:search){
            if(inside(ctx,point,yl+dy,m)){return true;} // 1.3.1.1.1 looking for steep floor
        }
        return false;
    }
    // 1.3.1.1.1 looking for steep floor
    private boolean inside(SurfCont ctx,IntPair XZ, int yl,boolean m) {
        TreeMap<Integer, Integer> levels = ctx.global.get(XZ);
        if (levels == null || levels.isEmpty()) {
            return false;
        }
        Integer yf = m?levels.higherKey(yl):levels.lowerKey(yl);
        if (yf==null){return false;}
        Integer i = levels.get(yf);
        if (i==null) {return false;}
        return m?i>32:i<-32;
    }
    private boolean wallch2(SurfCont ctx,int x,int z,int yl,boolean m) {
        int dy=m?-4:4;
        List<IntPair> search = Arrays.asList(
                new IntPair(x-2,z),
                new IntPair(x+2,z),
                new IntPair(x,z-2),
                new IntPair(x,z+2)
        );
        for(IntPair point:search){
            if(!inside2(ctx,point,yl+dy,m)){return true;}
        }
        return false;
    }
    private boolean inside2(SurfCont ctx,IntPair XZ, int yl,boolean m) {
        TreeMap<Integer, Integer> levels = ctx.global.get(XZ);
        if (levels == null || levels.isEmpty()) {
            return false;
        }
        Integer yf = m?levels.higherKey(yl):levels.lowerKey(yl);
        if (yf==null){return false;}
        Integer i = levels.get(yf);
        if (i==null) {return false;}
        return m?i>0:i<0;
    }
    private void edgeUpdate(Set<BlockPos> initial, Map<Integer, List<BlockPos>> biomeMap, boolean m, SurfCont ctx, Map<BlockPos, List<BlockPos>> mapS,int i) {
        Set<BlockPos> use = new HashSet<>(initial);
        Set<BlockPos> next = new HashSet<>();

        for (int n = 15; n > 0; --n) {
            List<BlockPos> list = biomeMap.get(n);
            if (list != null) {
                for (BlockPos pos : list) {
                    if (mapS.containsKey(pos)) use.add(pos);
                }
            }

            for (BlockPos point : use) {
                if (point == null) continue;
                IntPair XZ = new IntPair(point.getX(), point.getZ());
                TreeMap<Integer, Integer> floor = ctx.global.get(XZ);
                if (floor == null) continue;
                int yl = point.getY();
                Integer val = floor.get(yl);
                if (val == null) continue;
                switch(i){
                    case 0:
                        if (Math.abs(17 - val) > n) continue;
                        floor.put(yl, m ? 17 - n : -17 + n);break;
                    case 1:
                        if (Math.abs(val) > 17 || 17 - Math.abs(val) > n - 1) continue;
                        floor.put(yl, m ? 34 - val : -34 + val);break;
                    case 2:
                        if (Math.abs(val) > 17) continue;
                        floor.put(yl, m ? 34 - val : -34 + val);break;
                }
                next.addAll(mapS.get(point));
            }

            Set<BlockPos> temp = use;
            use = next;
            next = temp;
            next.clear();
        }
    }
    // 1.3.2 determining i-values for smooth mesh
    private void calcsmooth(SurfCont ctx,
                            Map<BlockPos, List<BlockPos>> mapS,
                            Map<BlockPos, List<BlockPos>> outS,
                            Map<BlockPos, List<BlockPos>> inS,
                            boolean m
    ) {
        Set<BlockPos> next = new HashSet<>();
        edgeUpdate(outS.keySet(), ctx.biomeO, m, ctx, mapS,0);
        edgeUpdate(inS.keySet(), Collections.emptyMap(), m, ctx, mapS,1);
        edgeUpdate(Collections.emptySet(),ctx.biomeI, m, ctx, mapS,2);
    }
    // 1.3.3 Calculatind steep mesh
    private void getsteep(SurfCont ctx,Map<BlockPos,List<BlockPos>> wall,boolean m){
        Map<BlockPos, List<BlockPos>> updated = new HashMap<>();
        for (BlockPos point:wall.keySet()){
            int x=point.getX();int z= point.getZ();int y=point.getY();
            List<IntPair> search = search(x,z);
            List<BlockPos> neighbours=new ArrayList<>();
            List<IntPair> scrap=new ArrayList<>();
            for (IntPair pos:search){
                TreeMap<Integer,Integer>pairs=ctx.global.get(pos);
                if (pairs==null){continue;}
                Integer yw=m?pairs.higherKey(y):pairs.lowerKey(y);
                if(yw==null){scrap.add(pos);continue;}
                Integer iw=pairs.get(yw);
                if(iw==null){scrap.add(pos);continue;}
                if(m?iw>32:iw<-32){
                    neighbours.add(new BlockPos(pos.first(),yw,pos.second()));
                }else{scrap.add(pos);}
            }
            for (IntPair pos:scrap){
                TreeMap<Integer,Integer>pairs=ctx.global.get(pos);
                if (pairs==null){continue;}
                Integer yw=m?pairs.lowerKey(y):pairs.higherKey(y);
                if(yw==null){continue;}
                Integer iw=pairs.get(yw);
                if(iw==null){continue;}
                if(m?iw>32:iw<-32){
                    neighbours.add(new BlockPos(pos.first(),yw,pos.second()));
                }
            }
            updated.put(point, neighbours);
        }
        wall.clear();
        wall.putAll(updated);
    }
    // 1.3.4 Steep calcutaions. Not sure if it's workking as intended since I lost track.

    private void calcsteep(SurfCont ctx,Map<BlockPos,List<BlockPos>> wall,Map<BlockPos,List<BlockPos>> edgei,boolean m){
        Map<BlockPos,List<BlockPos>> edge=new HashMap<>(edgei);
        for (int n=16;n>0;--n){//push calculation further
            if (edge.isEmpty())return;
            Map<BlockPos,List<BlockPos>> upd=new HashMap<>();
            for (BlockPos start:edge.keySet()){// main cycle
                Map<Integer,Integer>ydata=ctx.global.get(new IntPair(start.getX(),start.getZ()));
                if (ydata==null)continue;
                int ys=start.getY();
                Integer isi=Math.abs(ydata.get(ys));
                if (isi==null)continue;
                Integer is=isi<33?15-Math.abs(isi-17):isi-33;// I decided formulas on a go. It only looks good. Too tired to fully calculate this crap.
                if(wall.get(start)==null)continue;
                for(BlockPos neig:wall.get(start)){
                    if(neig==null)continue;
                    int yn=neig.getY();
                    int dy=m?neig.getY()-ys:ys-neig.getY();
                    if(dy<0)dy=0;
                    IntPair XZn=new IntPair(neig.getX(),neig.getZ());
                    Integer it=ctx.global.get(XZn).get(yn);
                    if (it==null)continue;
                    Integer i=it<33?Math.abs(it-17):it-33;
                    if (is-dy<i)continue;
                    TreeMap <Integer,Integer>ydata2=ctx.global.get(XZn);
                    int ifin=(m?1:-1)*(is-dy+33);
                    ydata2.put(yn,ifin);
                    upd.put(neig,wall.get(neig));
                }
            }
            edge.clear();
            edge.putAll(upd);
        }
    }
    // 2 Cool glacier thing for testing.
    private void test(int i,BlockPos pos,SurfCont ctx) {
        String biome=ctx.w.getRegistryManager().get(RegistryKeys.BIOME).getId(ctx.w.getBiome(pos).value()).toString();
        Map<Integer,RegistryEntry<PlacedFeature>> index=ctx.Ftypes.get(biome);
        if(index==null)return;
        RegistryEntry<PlacedFeature> feature=index.get(i);
        if(feature==null)return;
        ChunkGenerator generator = ctx.w.toServerWorld().getChunkManager().getChunkGenerator();
        feature.value().generate(ctx.w, generator, ctx.r, pos);
    }
    private void  geow(SurfCont ctx){
        ChunkGenerator generator = ctx.w.toServerWorld().getChunkManager().getChunkGenerator();
        List<BlockPos> positions = new ArrayList<>();
        positions.addAll(ctx.wallC.keySet());
        positions.addAll(ctx.wallF.keySet());
        Collections.shuffle(positions, new java.util.Random());
        for (int i = 0; i < Math.min(ctx.wall.i, positions.size()); ++i) {
            BlockPos pos = positions.get(i);
            if (ctx.w.getChunk(pos).equals(ctx.chunk)) {
                ctx.wall.feature().value().generate(ctx.w, generator, ctx.r, pos);
            }
        }
    }
}