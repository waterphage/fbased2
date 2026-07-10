package com.waterphage.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import com.sun.jna.StringArray;
import com.waterphage.meta.ChunkExtension;
import com.waterphage.meta.FBNMesh;
import com.waterphage.meta.FBXZMap;
import com.waterphage.meta.IntPair;
import com.waterphage.worldgen.ModRules;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
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
import org.spongepowered.asm.mixin.injection.struct.InjectorGroupInfo;

import java.util.*;

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
    private static final String[] MESH_NAMES = {
            "mapSF", "inSF", "outSF", "wallF",
            "mapSC", "inSC", "outSC", "wallC"
    };
    private static final Integer[] banpos= {
            0,0, 0,1, 0,2, 0,3, 0,4, 0,5, 0,6, 0,7, 0,8, 0,9, 0,10, 0,11, 0,12, 0,13, 0,14, 0,15,
            15,0, 15,1, 15,2, 15,3, 15,4, 15,5, 15,6, 15,7, 15,8, 15,9, 15,10, 15,11, 15,12, 15,13, 15,14, 15,15,
            1,0, 2,0, 3,0, 4,0, 5,0, 6,0, 7,0, 8,0, 9,0, 10,0, 11,0, 12,0, 13,0, 14,0,
            1,15, 2,15, 3,15, 4,15, 5,15, 6,15, 7,15, 8,15, 9,15, 10,15, 11,15, 12,15, 13,15, 14,15,
    };
    private class SurfCont {
        Chunk chunk;
        private FBNMesh[] meshes = new FBNMesh[MESH_NAMES.length];
        private Set<Long> banned = new HashSet<>();
        private LongArrayList bannedxyz =new LongArrayList();
        private StructureWorldAccess w;
        private Random r;
        private Map<String,BiomeValue> biomemap;
        private Wall wall;private Double bE;
        private Long2IntMap global = new Long2IntOpenHashMap();
        private FBXZMap globXZ = new FBXZMap();
        private LongArrayList[] biome=new LongArrayList[30];
        private Map<String,Map<Integer,RegistryEntry<PlacedFeature>>> Ftypes;
        private int xi;
        private int zi;
        private int yT;
        private float random(long pos){
            long seed = pos ^ w.getSeed() ^ ((FBXZMap.xL(pos)*31L) + (FBXZMap.zL(pos)*17L));
            java.util.Random random = new java.util.Random(seed);
            return random.nextFloat();
        }

        private SurfCont(FeatureContext<SurfaceConfig> ctx) {
            for (int i = 0; i < meshes.length; i++) {meshes[i] = new FBNMesh();}
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
        placer(ctx);// 1 holds all math stores placement positions and their indexes
        return true;
    }
    // 1 Placement positions calculation
    private void placer(SurfCont ctx){
        ChunkPos chunkPos = ctx.w.getChunk(new BlockPos(ctx.xi,ctx.yT,ctx.zi)).getPos();
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;
        global(chunkX,chunkZ,ctx); // Surface data reading
        neighbours(ctx); // 1.3 Edge calculation
        ChunkExtension ext=(ChunkExtension)ctx.chunk;
        Long2IntMap local= ext.getCustomMap();
        for (Long Lpos:local.keySet()){
            BlockPos.Mutable pos = BlockPos.fromLong(Lpos).mutableCopy();
            Integer i = ctx.global.get(Lpos);
            //System.out.println("Value of "+i+", X: must "+FBXZMap.xL(Lpos)+" done "+pos.getX()+", Y: must "+FBXZMap.yL(Lpos)+" done "+pos.getY()+", Z: must "+FBXZMap.zL(Lpos)+" done "+pos.getZ());
            test(i,pos,ctx);
            local.put(Lpos,i);
        }
        geow(ctx);
    }
    public static void global(int centerChunkX, int centerChunkZ,SurfCont ctx) {
        int id=0;
        for (FBNMesh mesh : ctx.meshes){mesh.chunk().put(0,0);}
        ctx.globXZ.chunk().put(0,0);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                id += 1;
                int chunkX = centerChunkX + dx;
                int chunkZ = centerChunkZ + dz;
                Chunk chunk = ctx.w.getChunk(chunkX, chunkZ, ChunkStatus.EMPTY, false);
                if (!(chunk instanceof ChunkExtension ext)) continue;
                ctx.global.putAll(ext.getCustomMap());
                ctx.globXZ.addAll(ext.getXZmap());
                ext.setPos(chunk.getPos().getCenterAtY(0));
                ctx.globXZ.chunk().put(id, ctx.globXZ.keyset().size() - 1); //marking chunks in a global data (probably not needed)
                BlockPos org = chunk.getPos().getStartPos();
                load(id, ctx, ext, org);  // 1.1.1 Loading data from chunk to context
            }
        }
        id=0;
        while (id<ctx.bannedxyz.size()/2){edgeCalc(ctx.bannedxyz.getLong(id),ctx.bannedxyz.getLong(id+1)>0,ctx);id+=2;}
        for (FBNMesh mesh : ctx.meshes){mesh.chunk().put(10,mesh.keyset().size()-1);}// I was never sure how to save data back, so if simple approach wil not succeed, I'll try to get from global data.
        ctx.globXZ.chunk().put(10,ctx.globXZ.keyset().size()-1);
    }
    private static void load(int id,SurfCont ctx,ChunkExtension ext,BlockPos org) {
        if (ext.getNMesh("mapSF").keyset().isEmpty()){calc(id,ctx,ext,org);return;} // 1.1.2 Generating local data
        for (int i=0;i<8;++i){
            FBNMesh ins=ctx.meshes[i];
            ins.addAll(ext.getNMesh(MESH_NAMES[i]));
            ins.chunk().put(id,ins.keyset().size()); //marking chunks in a global data (probably not needed
        }
        for (long l : ext.getNMesh("mapSF").keyset()){ctx.global.put(l,17);}
        for (long l : ext.getNMesh("mapSC").keyset()){ctx.global.put(l,-17);}
        for (long l : ext.getNMesh("wallF").keyset()){ctx.global.put(l,33);}
        for (long l : ext.getNMesh("wallC").keyset()){ctx.global.put(l,-33);}
    }
    private static void calc(int id,SurfCont ctx,ChunkExtension ext,BlockPos org){
        FBXZMap xzm=ext.getXZmap();
        Long2IntMap vls = ext.getCustomMap();
        int xo=org.getX();
        int zo=org.getZ();
        for(int i=0;i< banpos.length/2;++i){
            ctx.banned.add(FBXZMap.xzL(xo+banpos[i],zo+banpos[i+1]));
        }
        for(long xz:xzm.keyset()){
            if (ctx.banned.contains(xz)){ // 1.1.3 Splitting calculation (banned positions should not be written)
                int idk=xzm.indexMap().get(xz);
                int start = xzm.helper().getInt(idk);
                int end = (idk + 1 < xzm.helper().size()) ? xzm.helper().getInt(idk + 1) : xzm.values().size();
                for (int y=start;y<end;y++){
                    Long xyz=FBXZMap.L(xz,y);
                    ctx.bannedxyz.add(xyz);
                    ctx.bannedxyz.add((long)vls.get(FBXZMap.L(xz,y)));
                }
            }
            int idk=xzm.indexMap().get(xz);
            int xl=FBXZMap.xL(xz);
            int zl=FBXZMap.zL(xz);
            int start = xzm.helper().getInt(idk);
            int end = (idk + 1 < xzm.helper().size()) ? xzm.helper().getInt(idk + 1) : xzm.values().size();
            for (int y=start;y<end;y++){
                long xyz=FBXZMap.L(xz,y);
                boolean m=vls.get(FBXZMap.L(xz,y))>0;
                basicMap(xyz,xl,y,zl,ctx,ext,xzm,m);
            }
        }
        ext.count();
        Chunk ch=ctx.w.getChunk(ext.getPos());
        ch.setNeedsSaving(true); // 1.1.2 This should write new data?
        ch.needsSaving();
    }
    private static final Integer[] src1= {1,0, -1,0, 0,1, 0,-1,};
    private static final Integer[] src2= {1,0, -1,0, 0,1, 0,-1, 1,1, -1,-1, 1,-1, -1,1,};
    private static void basicMap(long key,int x,int y,int z, SurfCont ctx,ChunkExtension ext,FBXZMap xzm,boolean m) {
        LongArrayList mapNeig = new LongArrayList();
        LongArrayList wallNeig = new LongArrayList();
        Integer[] src=src1;int crn=4;
        if (ctx.random(key) < 0.58095F){src=src2;crn=8;}
        int yl = m?y-1:y+1;
        for (int i=0;i<src.length/2;++i){
            int xl=x+src[i];int zl=z+src[i+1];
            int yg=xzm.search(xl,zl,yl,m);
            long xyz=FBXZMap.L(xl,yl,zl);
            if (m!=ext.getCustomMap().get(xyz)>0){continue;}
            if (Math.abs(yg-y)>1){mapNeig.add(xyz);}
            else {wallNeig.add(xyz);}
        }
        // Since I'm already having everything:
        if (mapNeig.size()>0) {
            if (m){ext.getNMesh("mapSF").add(key,mapNeig);}else {ext.getNMesh("mapSC").add(key,mapNeig);}
            ctx.global.put(key,m?17:-17); //Smooth surface filler value
        }else{
            ctx.global.put(key,m?33:-33); //Wall filler value
        }

        if (mapNeig.size()<crn){
            if (m){ext.getNMesh("wallF").add(key,wallNeig);}else {ext.getNMesh("wallC").add(key,wallNeig);}
            if (wallNeig.size()>0){
                if (m){ext.getNMesh("inSF").add(key,mapNeig);}else {ext.getNMesh("inSC").add(key,mapNeig);}
            } else{
                if (m){ext.getNMesh("outSF").add(key,mapNeig);}else {ext.getNMesh("outSC").add(key,mapNeig);}
            }
        }
    }

    private static void edgeCalc(long key,boolean m, SurfCont ctx) {
        LongArrayList mapNeig = new LongArrayList();
        LongArrayList wallNeig = new LongArrayList();
        Integer[] src=src1;int crn=4;
        if (ctx.random(key) < 0.58095F){src=src2;crn=8;}
        int x=FBXZMap.xL(key);
        int y=FBXZMap.yL(key);
        int z=FBXZMap.zL(key);
        FBXZMap xzm=ctx.globXZ;
        Long2IntMap map=ctx.global;
        FBNMesh[] meshes= ctx.meshes;
        int yl = m?y-1:y+1;
        for (int i=0;i<src.length/2;++i){
            int xl=x+src[i];int zl=z+src[i+1];
            int yg=xzm.search(xl,zl,yl,m);
            long xyz=FBXZMap.L(xl,yl,zl);
            if (m!=map.get(xyz)>0){continue;}
            if (Math.abs(yg-y)>1){mapNeig.add(xyz);}
            else {wallNeig.add(xyz);}
        }
        //            0="mapSF", 1="inSF", 2="outSF", 3="wallF",4="mapSC", 5-"inSC", 6="outSC", 7="wallC"\
        if (mapNeig.size()>0){
            meshes[0+(m?0:4)].add(key,mapNeig);
            map.put(key,m?17:-17);
        }else {
            map.put(key,m?33:-33);
        }
        if (mapNeig.size()<crn){
            meshes[3+(m?0:4)].add(key,wallNeig);
            if (wallNeig.size()>0){
                meshes[1+(m?0:4)].add(key,mapNeig);
            } else{
                meshes[2+(m?0:4)].add(key,mapNeig);
            }
        }
    }
    //1.2 Edge detection and calculation
    void neighbours(SurfCont ctx){
        FBNMesh biom=new FBNMesh();biom.addAll(ctx.meshes[0]);biom.addAll(ctx.meshes[4]);
        for (Long org:biom.keyset()){biomeBorder(ctx,org,biom.get(org));} // biome effects
        calcsmooth(ctx,true); // determining smooth values
        calcsmooth(ctx,false);
        calcsteep(ctx,true);
        calcsteep(ctx,false);

    }
    private void biomeBorder(SurfCont ctx,Long org,LongArrayList local){
        String orgB=ctx.w.getRegistryManager().get(RegistryKeys.BIOME).getId(ctx.w.getBiome(BlockPos.fromLong(org)).value()).toString();
        if(lightcheck(ctx,org,local,orgB))return;
        for (Long neig:local){
            String neigB=ctx.w.getRegistryManager().get(RegistryKeys.BIOME).getId(ctx.w.getBiome(BlockPos.fromLong(neig)).value()).toString();
            String id=orgB+","+neigB;
            BiomeValue biom=ctx.biomemap.get(id);if(biom==null)continue;float rand=ctx.random(org);
            int n=Math.round(biom.out*rand*biom.chance);
            if(rand<1.0/biom.chance&&n>0){ctx.biome[n-1].add(org);} //ctx.biomeO
            n=Math.round(biom.in*rand*biom.chance2);
            if(rand<1.0/biom.chance2&&n>0){ctx.biome[n+14].add(org);} //ctx.biomeI
        }
    }
    private boolean lightcheck(SurfCont ctx,Long org,LongArrayList local,String orgB){
        Long xz=FBXZMap.xzL(org);int yo=FBXZMap.yL(org);boolean m=ctx.global.getOrDefault((long) org, 0) > 0; //should test it out later
        Integer ym=ctx.globXZ.search(xz,yo,m); if(ym==-9999)return false;
        for (Long neig:local){
            xz=FBXZMap.xzL(neig);yo=FBXZMap.yL(neig);
            ym=ctx.globXZ.search(xz,yo,m);if(ym!=-9999)continue;
            String key=orgB+",shadow";
            BiomeValue biom=ctx.biomemap.get(key);
            if(biom==null)continue;float rand=ctx.random(org);
            int n=Math.round(biom.out*rand*biom.chance);
            if(rand<1.0/biom.chance&&n>0){ctx.biome[n-1].add(org);} //ctx.biomeO
            n=Math.round(biom.in*rand*biom.chance2);
            if(rand<1.0/biom.chance2&&n>0){ctx.biome[n+14].add(org);} //ctx.biomeI
            return true;
        }
        return false;
    }
    private void edgeUpdate(LongArrayList initial, boolean m, SurfCont ctx, FBNMesh mapS,int i) {
        Set<Long> use = new HashSet<>(initial);
        Set<Long> next = new HashSet<>();

        for (int n = 15; n > 0; --n) {
            LongArrayList list = new LongArrayList();
            switch(i){
                case 0:
                    list = ctx.biome[n-1];break;
                case 1:
                    break;
                case 2:
                    list = ctx.biome[n+14];break;
            }
            if (list != null && !list.isEmpty()) { //another safeguard moment
                for (Long pos : list) {
                    if (mapS.keyset().contains(pos)) use.add(pos);
                }
            }
            for (Long point : use) {
                Integer val = ctx.global.get(point);
                if (val == null) continue;
                Integer f=val;
                switch(i){
                    case 0:
                        if (Math.abs(17 - val) > n) continue;
                        f=m ? 17 - n : -17 + n;break;
                    case 1:
                        if (Math.abs(val) > 17 || 17 - Math.abs(val) > n - 1) continue;
                        f=m ? 34 - val : -34 + val;break;
                    case 2:
                        if (Math.abs(val) > 17) continue;
                        f=m ? 34 - val : -34 + val;break;
                }
                ctx.global.put(point,f);
                next.addAll(mapS.get(point));
            }
            Set<Long> temp = use;
            use = next;
            next = temp;
            next.clear();
        }
    }
    // 1.3.2 determining i-values for smooth mesh
    private void calcsmooth(SurfCont ctx, boolean m) {
        Set<BlockPos> next = new HashSet<>();
        //            0="mapSF", 1="inSF", 2="outSF", 3="wallF",4="mapSC", 5-"inSC", 6="outSC", 7="wallC"\
        FBNMesh map=ctx.meshes[m?0:4];
        edgeUpdate(ctx.meshes[m?2:6].keyset(), m, ctx,map,0);
        edgeUpdate(ctx.meshes[m?1:5].keyset(), m, ctx,map,1);
        edgeUpdate(new LongArrayList(), m, ctx,map,2);
    }

    // 1.3.4 Steep calcutaions.

    private void calcsteep(SurfCont ctx,boolean m){
        //            0="mapSF", 1="inSF", 2="outSF", 3="wallF",4="mapSC", 5-"inSC", 6="outSC", 7="wallC"\
        FBNMesh edge=ctx.meshes[m?1:5];
        FBNMesh wall=ctx.meshes[m?3:7];
        for (int n=16;n>0;--n){//push calculation further
            if (edge.keyset().isEmpty())return;
            FBNMesh upd=new FBNMesh();
            for (Long start:edge.keyset()){// main cycle
                int ys=FBXZMap.yL(start);
                Integer rawGlobal = ctx.global.get(start);
                if (rawGlobal == null) continue;
                Integer isi=Math.abs(rawGlobal);
                if (isi==null)continue;
                Integer is=isi<33?15-Math.abs(isi-17):isi-33;// I decided formulas on a go. It only looks good. Too tired to fully calculate this crap.
                if(wall.get(start)==null)continue;
                for(Long neig:wall.get(start)){
                    if(neig==null)continue;
                    int yn=FBXZMap.yL(neig);
                    int dy=m?yn-ys:ys-yn;
                    if(dy<0)dy=0;
                    Integer it=ctx.global.get(neig);
                    if (it==null)continue;
                    Integer i=it<33?Math.abs(it-17):it-33;
                    if (is-dy<i)continue;
                    Integer ifin=(m?1:-1)*(is-dy+33);
                    ctx.global.put(neig,ifin);
                    upd.add(neig,wall.get(neig));
                }
            }
            edge.clear();
            edge.addAll(upd);
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
        List<Long> positions = new ArrayList<>();
        //            0="mapSF", 1="inSF", 2="outSF", 3="wallF",4="mapSC", 5-"inSC", 6="outSC", 7="wallC"\
        positions.addAll(ctx.meshes[3].keyset());
        positions.addAll(ctx.meshes[7].keyset());
        Collections.shuffle(positions, new java.util.Random());
        for (int i = 0; i < Math.min(ctx.wall.i, positions.size()); ++i) {
            BlockPos pos = BlockPos.fromLong(positions.get(i));
            if (ctx.w.getChunk(pos).equals(ctx.chunk)) {
                ctx.wall.feature().value().generate(ctx.w, generator, ctx.r, pos);
            }
        }
    }
}