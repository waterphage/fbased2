package com.waterphage.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import com.waterphage.Fbased;
import com.waterphage.meta.ChunkExtension;
import com.waterphage.meta.FBNMesh;
import com.waterphage.meta.FBXZMap;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
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
import org.jetbrains.annotations.NotNull;

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
    //public static final UnboundedMapCodec<Integer, RegistryEntry<PlacedFeature>> FB_INDEX_FEATURE_CODEC = Codec.unboundedMap(Codec.STRING.xmap(Integer::parseInt, Object::toString), PlacedFeature.REGISTRY_CODEC);
    //public static final UnboundedMapCodec<String, Map<Integer, RegistryEntry<PlacedFeature>>> FB_BIOME_FEATURE_CODEC = Codec.unboundedMap(Codec.STRING, FB_INDEX_FEATURE_CODEC);

    public static final Codec<Map<String, BiomeValue>> FB_BIOME_MAP_CODEC = Codec.unboundedMap(Codec.STRING, FB_BIOME_VALUE_CODEC);
    public static class SurfaceConfig implements FeatureConfig {
        public static final Codec<SurfaceConfig> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Codec.INT.fieldOf("tech_Y").forGetter(config -> config.yT),
                        Codec.INT.fieldOf("min").forGetter(config -> config.min),
                        Codec.INT.fieldOf("max").forGetter(config -> config.max),
                        Codec.INT.listOf().fieldOf("matrix").forGetter(config -> config.matrix),
                        FB_WALL_CODEC.listOf().fieldOf("wall").forGetter(config -> config.wall),
                        FB_BIOME_MAP_CODEC.fieldOf("biome_relations").forGetter(config -> config.biome)
                        //FB_BIOME_FEATURE_CODEC.fieldOf("biome_features").forGetter(config -> config.feature)
                ).apply(instance, SurfaceConfig::new));
        private Integer min;
        private Integer yT;
        private Integer max;
        private List<Integer> matrix;
        private List<Wall> wall;
        private Map<String,BiomeValue> biome;
        private Map<String,Map<Integer,RegistryEntry<PlacedFeature>>> feature;

        SurfaceConfig(Integer yT,Integer min, Integer max,List<Integer> matrix,List<Wall> wall,Map<String,BiomeValue>biome
                      //Map<String,Map<Integer,RegistryEntry<PlacedFeature>>> feature
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
            "mapSC", "inSC", "outSC", "wallC",
            "rawSF", "rawSC", "walRF", "wallRC"
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
        private LongArrayList toRefine = new LongArrayList();
        private IntArrayList toSave = new IntArrayList();
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
            for (int i = 0; i < biome.length; i++) {
                biome[i] = new LongArrayList();
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
        global(chunkX,chunkZ,ctx); // 1.1 Surface data reading
        refine(ctx); // 1.2 Neighbour mesh sampling
        export(chunkX,chunkZ,ctx);
        neighbours(ctx); // 1.3 Edge calculation

        ChunkExtension ext=(ChunkExtension)ctx.chunk;
        Long2IntMap local= ext.getCustomMap();
        for (Long Lpos:local.keySet()){
            BlockPos.Mutable pos = BlockPos.fromLong(Lpos).mutableCopy();
            Integer i = ctx.global.get(Lpos);
            test(i,pos,ctx);
        }
        geow(ctx);
    }
    // 1.1 Surface data reading it goes for every chunk to read raw data and write into global data
    public static void global(int centerChunkX, int centerChunkZ,SurfCont ctx) {
        int id=0;
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
                BlockPos org = chunk.getPos().getStartPos();
                load(id, ctx, ext, org);  // 1.1.1 Loading neighbour mesh chunk data.
            }
        }
        // 1.1.3 Creating unrefined mesh data of chunk edges. They should not be saved.
        id=0;while (id<ctx.bannedxyz.size()/2){edgeBan(ctx.bannedxyz.getLong(id*2),ctx.bannedxyz.getLong(id*2+1)>0,ctx);id+=1;}
    }
    private static void load(int id,SurfCont ctx,ChunkExtension ext,BlockPos org) {
        if (ext.getNMesh("mapSF").keyset().isEmpty()){calc(id,ctx,ext,org);return;} // 1.1.2 Generating local data
        int xo=org.getX();int zo=org.getZ();
        for(int i=0;i< banpos.length/2;++i){
            Long xzb=FBXZMap.xzL(xo+banpos[i*2],zo+banpos[i*2+1]);
            ctx.banned.add(xzb);
            Long2IntMap vls = ext.getCustomMap();
            FBXZMap xzm=ext.getXZmap();
            int idk=xzm.indexMap().get(xzb);
            int start = xzm.helper().getInt(idk);
            int end = (idk + 1 < xzm.helper().size()) ? xzm.helper().getInt(idk + 1) : xzm.values().size();
            for (int idy=start;idy<end;idy++){
                int y = xzm.values().getInt(idy);
                Long xyz=FBXZMap.L(xzb,y);
                ctx.bannedxyz.add(xyz);
                ctx.bannedxyz.add((long)vls.get(xyz));
            }
        }
        for (int i=0;i<MESH_NAMES.length;++i){
            if (ext.getNMesh(MESH_NAMES[i]).keyset().isEmpty())continue;
            FBNMesh ins=ctx.meshes[i];
            FBNMesh imp=ext.getNMesh(MESH_NAMES[i]);
            ins.addAll(imp);
            if (i%4==0){int idm=i/4+8;ctx.meshes[idm].addAll(imp);}
            if (i%4==3){int idm=i/4+10;ctx.meshes[idm].addAll(imp);}
        }
    }
    private static void calc(int id,SurfCont ctx,ChunkExtension ext,BlockPos org){
        ctx.toSave.add(id);
        FBXZMap xzm=ext.getXZmap();
        Long2IntMap vls = ext.getCustomMap();
        int xo=org.getX();
        int zo=org.getZ();
        for(int i=0;i< banpos.length/2;++i){
            ctx.banned.add(FBXZMap.xzL(xo+banpos[i*2],zo+banpos[i*2+1]));
        }
        for(long xz:xzm.keyset()){
            if (ctx.banned.contains(xz)){ // 1.1.2.1 Splitting calculation (banned positions should not be written)
                int idk=xzm.indexMap().get(xz);
                int start = xzm.helper().getInt(idk);
                int end = (idk + 1 < xzm.helper().size()) ? xzm.helper().getInt(idk + 1) : xzm.values().size();
                for (int idy=start;idy<end;idy++){
                    int y = xzm.values().getInt(idy);
                    Long xyz=FBXZMap.L(xz,y);
                    ctx.bannedxyz.add(xyz);
                    ctx.bannedxyz.add((long)vls.get(xyz));
                }
                continue;
            }
            int idk=xzm.indexMap().get(xz);
            int xl=FBXZMap.xL(xz);
            int zl=FBXZMap.zL(xz);
            int start = xzm.helper().getInt(idk);
            int end = (idk + 1 < xzm.helper().size()) ? xzm.helper().getInt(idk + 1) : xzm.values().size();
            for (int idy=start;idy<end;++idy){
                int y = xzm.values().getInt(idy);
                long xyz=FBXZMap.L(xz,y);
                boolean m=vls.get(FBXZMap.L(xz,y))>0;
                basicMap(xyz,xl,y,zl,ctx,ext,xzm,m); // 1.1.2.2 Single point calculation
            }
        }
        ext.count(); // 1.1.2.3 Marking chunk for update (useless for now)
        Chunk ch=ctx.w.getChunk(ext.getPos());
        ch.setNeedsSaving(true);
        ch.needsSaving();
    }
    // 1.1.2.2 and 1.1.3 hepler stuff
    private static final Integer[] src1= {1,0, -1,0, 0,1, 0,-1,};
    private static final Integer[] src2= {1,1, -1,-1, 1,-1, -1,1,};
    private static void mark(int crn,LongArrayList neig,boolean m,Long key, SurfCont ctx){
        Integer val;
        if (crn < 4) {
            ctx.meshes[3].add(key, neig);
        } else {
            int t=m?8:9;ctx.meshes[t].add(key, neig);val=m?17:-17;
            ctx.global.put(key,val);//Smooth surface filler value
            ctx.toRefine.add(key);ctx.toRefine.add(t);
        }

    }
    private static int test(boolean m,int x,int y,int z,LongArrayList neig,SurfCont ctx){
        int yl = m?y-2:y+2;int crn=0;
        int p=16;
        for (int i=0;i<src1.length/2;++i){
            int xl=x+src1[i*2];int zl=z+src1[i*2+1];
            int yg=ctx.globXZ.search(xl,zl,yl,m);
            long xyz=FBXZMap.L(xl,yg,zl);
            if (m!=ctx.global.get(xyz)>0||yg==-9999){continue;}
            if (Math.abs(yg-y)<2){crn+=1;}
            else{p=Math.min(p,(m?yg-y:y-yg)-1);if(p>0){crn+=1;}}
            neig.add(xyz);
        }
        Long xyz=FBXZMap.L(x,y,z);
        if(p<16&&p>0){ctx.biome[p].add(xyz);ctx.biome[29-p/2].add(xyz);}
        biomeBorder(ctx,xyz,neig);
        return crn;
    }
    // 1.1.2.2 Single point calculation
    private static void basicMap(long key,int x,int y,int z, SurfCont ctx,ChunkExtension ext,FBXZMap xzm,boolean m) {
        LongArrayList neig = new LongArrayList();
        int crn = test(m,x,y,z,neig,ctx);
        mark(crn,neig,m,key,ctx);
    }
    // 1.1.3 Banned points calculation
    private static void edgeBan(long key,boolean m, SurfCont ctx) {
        LongArrayList neig = new LongArrayList();
        int x=FBXZMap.xL(key);
        int y=FBXZMap.yL(key);
        int z=FBXZMap.zL(key);
        int crn = test(m,x,y,z,neig,ctx);
        mark(crn,neig,m,key,ctx);
    }
    //1.2 Mesh refining
    private void refine(SurfCont ctx){
        for (int i=0;i<ctx.toRefine.size()/2;++i){
            Long key=ctx.toRefine.get(i*2);
            Integer t= Math.toIntExact(ctx.toRefine.get(i * 2 + 1));
            FBNMesh mesh=ctx.meshes[t];
            LongArrayList chk=mesh.get(key);
            setsmooth(key,chk,t,ctx);
        }
    }
    private static final int[] CTYPE = {0, 4, 3, 7};
    private static int Ct(Integer t) {
        return CTYPE[t - 8];
    }
    private static void setsmooth(Long key, LongArrayList chk, Integer t, SurfCont ctx){
        //8="rawSF", 9="rawSC", 10="walRF", 11="wallRC"
        LongArrayList map=new LongArrayList();
        LongArrayList wall=new LongArrayList();
        boolean m=t%2==0;int y=FBXZMap.yL(key);
        for (Long n:chk){
            if(ctx.meshes[t].keyset().contains(n)){map.add(n);}
            else{int yl=FBXZMap.yL(n);if (m?yl>y-1:yl<y+1){wall.add(n);}}
        }
        //            0="mapSF", 1="inSF", 2="outSF", 3="wallF",4="mapSC", 5-"inSC", 6="outSC", 7="wallC"\
        if (map.size()<4){ctx.meshes[Ct(t)+2].add(key,map);}
        else{
            if (ctx.random(key) < 0.58095F*0.5F){
                int x=FBXZMap.xL(key);int z=FBXZMap.zL(key);
                for (int i=0;i<src2.length/2;++i) {
                    int xl=x+src2[i*2];int zl=z+src2[i*2+1];
                    int yl=m?y-2:y+2;
                    int yg=ctx.globXZ.search(xl,zl,yl,m);
                    Long p=FBXZMap.L(xl,yg,zl);
                    if (ctx.meshes[t].keyset().contains(p)){map.add(p);}
                }
            }
        }
        ctx.meshes[Ct(t)].add(key,map);
    }
    private static final Integer[] exp= {0,4,3,7};
    public static void export(int centerChunkX, int centerChunkZ,SurfCont ctx) {
        int id=0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                id += 1;
                if (!ctx.toSave.contains(id)){continue;}
                LongArrayList keyset=new LongArrayList();
                int chunkX = centerChunkX + dx;
                int chunkZ = centerChunkZ + dz;
                Chunk chunk = ctx.w.getChunk(chunkX, chunkZ, ChunkStatus.EMPTY, false);
                if (!(chunk instanceof ChunkExtension ext)) continue;
                Long2IntMap Cmap=ext.getCustomMap();
                for (long xyz:Cmap.keySet()){
                    Long xz=FBXZMap.xzL(xyz);
                    if (ctx.banned.contains(xz)){continue;}
                    Cmap.put(xyz,ctx.global.get(xyz));
                    keyset.add(xyz);
                    for (int Im=0;Im<MESH_NAMES.length;++Im){
                        FBNMesh map=ext.getNMesh(MESH_NAMES[Im]);
                        if (!map.keyset().contains(xyz))continue;
                        map.add(xyz,ctx.meshes[Im].get(xyz));
                    }
                }
            }
        }
    }

    //1.3 Edge detection and calculation
    private void neighbours(SurfCont ctx){
        calcsmooth(ctx,true); // determining smooth values
        calcsmooth(ctx,false);
    }
    // 1.3.2 determining i-values for smooth mesh
    private void calcsmooth(SurfCont ctx, boolean m) {
        //            0="mapSF", 1="inSF", 2="outSF", 3="wallF",4="mapSC", 5-"inSC", 6="outSC", 7="wallC"\
        FBNMesh map = ctx.meshes[m?0:4];
        edgeUpdate1(ctx.meshes[m?2:6].keyset(), m, ctx,map);//ctx.meshes[m?2:6].keyset()
        //edgeUpdate2(ctx.meshes[m?1:5].keyset(), m, ctx,map);
        edgeUpdate3(new LongArrayList(), m, ctx,map);
    }
    private void edgeUpdate1(LongArrayList initial, boolean m, SurfCont ctx, FBNMesh mapS) {
        Set<Long> use = new HashSet<>(initial);
        Set<Long> next = new HashSet<>();
        Set<Long> ban=new HashSet<>();
        for (int n = 15; n > 0; --n) {
            LongArrayList list = new LongArrayList();
            list = ctx.biome[n-1];
            if (list != null && !list.isEmpty()) { //another safeguard moment
                for (Long pos : list) {
                    if (ban.contains(pos))continue;
                    if (mapS.keyset().contains(pos)) use.add(pos);
                }
            }
            for (Long point : use) {
                Integer val = Math.abs(ctx.global.get(point));
                if (val == null) continue;
                Integer f=val;
                if (Math.abs(17 - val) > n) continue;
                f=m ? 17 - n : -17 + n;
                ctx.global.put(point,f);
                next.addAll(mapS.get(point));
            }
            ban.addAll(use);
            next.removeAll(ban);
            Set<Long> temp = use;
            use = next;
            next = temp;
            next.clear();
        }
    }
    /**
    private void edgeUpdate2(LongArrayList initial, boolean m, SurfCont ctx, FBNMesh mapS) {
        Set<Long> use = new HashSet<>(initial);
        Set<Long> next = new HashSet<>();

        for (int n = 15; n > 0; --n) {
            LongArrayList list = new LongArrayList();
            for (Long point : use) {
                Integer val = Math.abs(ctx.global.get(point));
                if (val == null) continue;
                Integer f=val;
                if (val > 17 || 17 - val > n - 1) continue;
                f=m ? 34 - val : -34 + val;
                ctx.global.put(point,f);
                next.addAll(mapS.get(point));
            }
            Set<Long> temp = use;
            use = next;
            next = temp;
            next.clear();
        }
    }*/
    private void edgeUpdate3(LongArrayList initial, boolean m, SurfCont ctx, FBNMesh mapS) {
        Set<Long> use = new HashSet<>(initial);
        Set<Long> next = new HashSet<>();
        Set<Long> ban=new HashSet<>();
        for (int n = 15; n > 0; --n) {
            LongArrayList list = new LongArrayList();
            list = ctx.biome[n+14];
            if (list != null && !list.isEmpty()) { //another safeguard moment
                for (Long pos : list) {
                    if (ban.contains(pos))continue;
                    if (mapS.keyset().contains(pos)) use.add(pos);
                }
            }
            for (Long point : use) {
                Integer val = Math.abs(ctx.global.get(point));
                if (val == null) continue;
                Integer f=val;
                if (val > 17) continue;
                f=m ? 34 - val : -34 + val;
                ctx.global.put(point,f);
                next.addAll(mapS.get(point));
            }
            ban.addAll(use);
            next.removeAll(ban);
            Set<Long> temp = use;
            use = next;
            next = temp;
            next.clear();
        }
    }
    private static void biomeBorder(SurfCont ctx, Long org, LongArrayList local){
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
    private static boolean lightcheck(SurfCont ctx, Long org, LongArrayList local, String orgB){
        Long xz=FBXZMap.xzL(org);int yo=FBXZMap.yL(org);boolean m=ctx.global.getOrDefault((long) org, 0) > 0; //should test it out later
        Integer ym=ctx.globXZ.search(xz,yo,m); if(ym==-9999)return false;
        for (Long neig:local){
            xz=FBXZMap.xzL(neig);yo=FBXZMap.yL(neig);
            ym=ctx.globXZ.search(xz,yo,m);if(ym!=-9999)continue;
            String key=orgB+",shadow";
            BiomeValue biom=ctx.biomemap.get(key); Long seed=ctx.w.getSeed();
            if(biom==null)continue;float rand=ctx.random(-(seed+org)*(seed+org));float rand2=ctx.random((seed-org)*(seed-org));
            int n=Math.round(biom.out*rand*biom.chance);
            if(rand<1.0/biom.chance&&n>0){ctx.biome[n-1].add(org);} //ctx.biomeO
            n=Math.round(biom.in*rand2*biom.chance2);
            if(rand2<1.0/biom.chance2&&n>0){ctx.biome[n+14].add(org);} //ctx.biomeI
            return true;
        }
        return false;
    }
    // 2 Surface feature placement
    private void test(int i,BlockPos pos,SurfCont ctx) {
        Identifier biom=ctx.w.getRegistryManager()
                .get(RegistryKeys.BIOME)
                .getId(ctx.w.getBiome(pos).value());
        Identifier id = Identifier.of(biom.getNamespace(),"cover/"+biom.getPath()+"/"+i);
        RegistryKey<PlacedFeature> key = RegistryKey.of(RegistryKeys.PLACED_FEATURE, id);
        RegistryEntry<PlacedFeature> feature = ctx.w.getRegistryManager()
                .get(RegistryKeys.PLACED_FEATURE)
                .getEntry(key)
                .orElse(null);
        if(feature==null)return;
        ChunkGenerator generator = ctx.w.toServerWorld().getChunkManager().getChunkGenerator();
        feature.value().generate(ctx.w, generator, ctx.r, pos);
    }
    // 3 Geology placement
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