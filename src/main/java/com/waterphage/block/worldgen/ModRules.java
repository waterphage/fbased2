package com.waterphage.block.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.waterphage.Fbased;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.VerticalSurfaceType;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.BlockSource;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.chunk.BlockColumn;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static java.lang.Math.abs;

public class ModRules extends MaterialRules {
    public static void registerrules() {
        Registry.register(Registries.MATERIAL_RULE, new Identifier(Fbased.MOD_ID, "geology_s"), GeologyS.CODEC.codec());
        Registry.register(Registries.MATERIAL_RULE, new Identifier(Fbased.MOD_ID, "geology_d"), GeologyD.CODEC.codec());
        Registry.register(Registries.MATERIAL_RULE, new Identifier(Fbased.MOD_ID, "geology"), Geology.CODEC.codec());
    }

    static <A> Codec<? extends A> register(Registry<Codec<? extends A>> registry, String id, CodecHolder<? extends A> codecHolder) {
        return Registry.register(registry, new Identifier(Fbased.MOD_ID, id), codecHolder.codec());
    }

    public interface MaterialRule extends Function<MaterialRuleContext, BlockStateRule> {
        Codec<MaterialRules.MaterialRule> CODEC = Registries.MATERIAL_RULE.getCodec().dispatch(materialRule -> materialRule.codec().codec(), Function.identity());

        static Codec<? extends MaterialRules.MaterialRule> registerAndGetDefault(Registry<Codec<? extends MaterialRules.MaterialRule>> registry) {
            ModRules.register(registry, "geology_s", ModRules.GeologyS.CODEC);
            ModRules.register(registry, "geology_d", ModRules.GeologyD.CODEC);
            return ModRules.register(registry, "geology", ModRules.Geology.CODEC);
        }

        CodecHolder<? extends MaterialRules.MaterialRule> codec();
    }

    public static ModRules.Geology condition(List<String> id_o,List<String> id_l,List<String> id_d,List<String> scale, List<Geology.GeoEntry> rock) {
        return new ModRules.Geology(id_o,id_l,id_d,scale, rock);
    }

    record Geology(List<String> id_o,List<String> id_l,List<String> id_d,List<String> scale, List<GeoEntry> rock) implements MaterialRules.MaterialRule {
        public static class GeoEntry {
            public static final Codec<GeoEntry> CODEC = RecordCodecBuilder.create(
                    instance -> instance.group(
                                    Codec.DOUBLE.listOf().fieldOf("c").forGetter(config -> config.coord),
                                    BlockStateProvider.TYPE_CODEC.fieldOf("m").forGetter(config -> config.mineral)
                            )
                            .apply(instance, GeoEntry::new)
            );
            public final List<Double> coord;
            public final  BlockStateProvider mineral;

            public GeoEntry(List<Double> coord, BlockStateProvider mineral) {
                this.coord = coord;
                this.mineral = mineral;
            }
        }
        static final CodecHolder<ModRules.Geology> CODEC = CodecHolder.of(
                RecordCodecBuilder.mapCodec(
                        instance -> instance.group(
                                        Codec.STRING.listOf().fieldOf("biome_noise_a").forGetter(ModRules.Geology::id_l),
                                        Codec.STRING.listOf().fieldOf("biome_noise").forGetter(ModRules.Geology::id_o),
                                        Codec.STRING.listOf().fieldOf("local_noise").forGetter(ModRules.Geology::id_d),
                                        Codec.STRING.listOf().fieldOf("mode").forGetter(ModRules.Geology::scale),
                                        GeoEntry.CODEC.listOf().fieldOf("types").forGetter(ModRules.Geology::rock)
                                )
                                .apply(instance, ModRules.Geology::new)
                )
        );



        public CodecHolder<Geology> Geology(CodecHolder<Geology> codec) {
            return CODEC;
        }

        @Override
        public CodecHolder<? extends MaterialRules.MaterialRule> codec() {
            return CODEC;
        }

        public MaterialRules.BlockStateRule apply(MaterialRules.MaterialRuleContext cont) {
            Chunk chunk = cont.chunk;
            HeightContext height = cont.heightContext;
            NoiseConfig noise = cont.noiseConfig;
            net.minecraft.util.math.random.Random random = Random.create();


            class Backup{
                public int xprev=-9999;
                public int zprev=-9999;
                public int ym;
                public double dist_os;
                public double dist_ls;
                public List<BlockStateProvider> stone;
            }
            Backup backup = new Backup();

            DoublePerlinNoiseSampler dist_o = noise.getOrCreateSampler(RegistryKey.of(RegistryKeys.NOISE_PARAMETERS, new Identifier(id_o.get(0),id_o.get(1))));
            DoublePerlinNoiseSampler dist_l = noise.getOrCreateSampler(RegistryKey.of(RegistryKeys.NOISE_PARAMETERS, new Identifier(id_l.get(0),id_l.get(1))));
            DoublePerlinNoiseSampler dist_d = noise.getOrCreateSampler(RegistryKey.of(RegistryKeys.NOISE_PARAMETERS, new Identifier(id_d.get(0),id_d.get(1))));
            List<Float> use = new ArrayList<>(Arrays.asList(1F,1F,1F,0.5F,0F,0.2F,0F,0F,5F,5F));

            int i=0;
            for (String elem : scale) {
                try {
                    Float value = Float.parseFloat(elem);
                    use.set(i,value);
                } catch (NumberFormatException nfe) {}
                i += 1;
            }
            Float os_xz=use.get(0); //offset layer noise xz scale
            Float ol_xz=use.get(1); //surface alignment noise xz scale
            Float od_xz=use.get(2); // displace noise scale
            Float s_p=use.get(3); //surface alignment rule constant
            Float s=use.get(4); //surface alignment rule noise power
            Float l_p=use.get(5); //layer filling base constant
            Float l=use.get(6); //layer filling noise power
            Float l_a=use.get(7); //layer filling power constant
            Float s_o=use.get(8); // displace power
            Float pool=use.get(9); // mineral pool scale

            List<List<Double>> map=new ArrayList<>();
            for (GeoEntry point : rock){
                map.add(point.coord);
            }
            int ymax = height.getHeight();
            return (x,y, z) -> {

                if (x!=backup.xprev||z!=backup.zprev){
                    backup.ym=chunk.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR_WG,x,z);
                    backup.stone=new ArrayList<>();
                    backup.dist_os=dist_o.sample(x*os_xz,0, z*os_xz);
                    backup.dist_ls=dist_l.sample(x*ol_xz,0, z*ol_xz);
                    List<List<Double>> map2 = new ArrayList<>(map);
                    for (int k=0; k<=pool;k++) {
                        Double dist_m =5D;
                        int j=0;
                        int ind=0;
                        for (List<Double> min : map2){
                            Double geo_o=min.get(0);
                            Double geo_l=min.get(1);
                            Double dist=Math.pow((backup.dist_os-geo_o),2)+Math.pow((backup.dist_ls-geo_l),2);
                            if (dist<=dist_m){
                                dist_m=dist;
                                ind=j;
                                map2.set(j,List.of(999D, 999D));
                            }
                            j+=1;
                        }
                        backup.stone.add(rock.get(ind).mineral);
                    }
                }
                double dist_od=dist_d.sample(x*od_xz, y*od_xz, z*od_xz);
                int ys = (int)(Math.round(backup.ym*(s_p+s*backup.dist_ls) + ymax*(1-s_p-s*backup.dist_ls)));
                int layer = (int)(Math.round(abs(y - ys-s_o*dist_od)*Math.pow(l_p,l_a+l*backup.dist_os))) % backup.stone.size();
                BlockPos pos = new BlockPos(x,y,z);
                backup.xprev=x;
                backup.zprev=z;
                return backup.stone.get(layer).get(random,pos);
            };
        }
    }

    public static ModRules.GeologyS condition(Float power,Float scale,List<BlockStateProvider> rock) {
        return new ModRules.GeologyS(power,scale,rock);
    }
    record GeologyS(Float power,Float scale,List<BlockStateProvider> rock) implements MaterialRules.MaterialRule {
        static final CodecHolder<ModRules.GeologyS> CODEC = CodecHolder.of(
                RecordCodecBuilder.mapCodec(
                        instance -> instance.group(
                                        Codec.FLOAT.fieldOf("power").forGetter(ModRules.GeologyS::power),
                                        Codec.FLOAT.fieldOf("scale").forGetter(ModRules.GeologyS::scale),
                                        BlockStateProvider.TYPE_CODEC.listOf().fieldOf("types").forGetter(ModRules.GeologyS::rock)
                                )
                                .apply(instance, ModRules.GeologyS::new)
                )
        );

        public CodecHolder<GeologyS> GeologyS(CodecHolder<GeologyS> codec) {
            return CODEC;
        }

        @Override
        public CodecHolder<? extends MaterialRules.MaterialRule> codec() {
            return CODEC;
        }

        public MaterialRules.BlockStateRule apply(MaterialRules.MaterialRuleContext cont) {
            HeightContext height = cont.heightContext;
            Chunk chunk = cont.chunk;
            net.minecraft.util.math.random.Random random = Random.create();

            int ymax = height.getHeight();
            return (x, y, z) -> {
                int ym = chunk.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR_WG,x,z);
                int ys = Math.round(ym*power + ymax*(1-power));
                int layer = Math.round(abs((y - ys)/scale)) % rock.size();
                BlockPos pos = new BlockPos(x,y,z);
                return rock.get(layer).get(random,pos);
            };
        }
    }

    public static ModRules.GeologyD condition(List<String> id_o,List<String> id_l,List<String> id_d,List<Float> scale_o,List<Float> scale_l,List<Integer> matrix, List<List<Integer>> goal,List<Float> bed, List<BlockStateProvider> rock) {
        return new ModRules.GeologyD(id_o,id_l,id_d,scale_o,scale_l, matrix, goal,bed, rock);
    }

    public static class Backup{
        public int xprev=-9999;
        public int zprev=-9999;
        public int ym;

        public ArrayList<Integer> point;
        public ArrayList<Integer> surf;
        public ArrayList<Integer> grnd;
        public double dist_os;
        public double dist_ls;

        public Backup apply(Backup b){
            return b;
        };

    }

    record GeologyD(List<String> id_o,List<String> id_l,List<String> id_d,List<Float> sc_o,List<Float> sc_l,List<Integer> matrix,List<List<Integer>> goal,List<Float> bed, List<BlockStateProvider> rock) implements MaterialRules.MaterialRule {
        static final CodecHolder<ModRules.GeologyD> CODEC = CodecHolder.of(
                RecordCodecBuilder.mapCodec(
                        instance -> instance.group(
                                        Codec.STRING.listOf().fieldOf("biome_noise_a").forGetter(ModRules.GeologyD::id_l),
                                        Codec.STRING.listOf().fieldOf("biome_noise").forGetter(ModRules.GeologyD::id_o),
                                        Codec.STRING.listOf().fieldOf("local_noise").forGetter(ModRules.GeologyD::id_d),
                                        Codec.FLOAT.listOf().fieldOf("offset").forGetter(ModRules.GeologyD::sc_o),
                                        Codec.FLOAT.listOf().fieldOf("width").forGetter(ModRules.GeologyD::sc_l),
                                        Codec.INT.listOf().fieldOf("matrix").forGetter(ModRules.GeologyD::matrix),
                                        Codec.INT.listOf().listOf().fieldOf("goal").forGetter(ModRules.GeologyD::goal),
                                        Codec.FLOAT.listOf().fieldOf("bedrock").forGetter(ModRules.GeologyD::bed),
                                        BlockStateProvider.TYPE_CODEC.listOf().fieldOf("types").forGetter(ModRules.GeologyD::rock)
                                )
                                .apply(instance, ModRules.GeologyD::new)
                )
        );
        public CodecHolder<GeologyD> GeologyD(CodecHolder<GeologyD> codec) {
            return CODEC;
        }
        @Override
        public CodecHolder<? extends MaterialRules.MaterialRule> codec() {
            return CODEC;
        }

        public Backup write(Backup backup,MaterialRules.MaterialRuleContext cont){
            Float xz_o=Float.parseFloat(id_o.get(2));
            Float xz_l=Float.parseFloat(id_l.get(2));
            int s_k=goal.size();int s_m=rock.size();
            int m0=matrix.get(0);int m1=matrix.get(1);
            Chunk chunk = cont.chunk;
            NoiseConfig noise = cont.noiseConfig;
            BlockPos.Mutable pos = cont.pos;
            int x=pos.getX();int z=pos.getZ();
            DoublePerlinNoiseSampler dist_o = noise.getOrCreateSampler(RegistryKey.of(RegistryKeys.NOISE_PARAMETERS, new Identifier(id_o.get(0),id_o.get(1))));
            DoublePerlinNoiseSampler dist_l = noise.getOrCreateSampler(RegistryKey.of(RegistryKeys.NOISE_PARAMETERS, new Identifier(id_l.get(0),id_l.get(1))));
            backup.ym=chunk.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR_WG,x,z);
            backup.dist_os=(0.5D+0.5D*dist_o.sample(x*xz_o,0, z*xz_o));
            backup.dist_ls=(0.5D+0.5D*dist_l.sample(x*xz_l,0, z*xz_l));
            backup.point = new ArrayList<Integer>();
            for (int k=0; k<s_k;k++) {
                backup.point.add((int)Math.round(abs((backup.dist_os*(m0-1)+goal.get(k).get(0)+(backup.dist_ls*(m1-1)+goal.get(k).get(1))*m0)))%s_m);
            }
            return backup;
        };
        public MaterialRules.BlockStateRule applyb(Backup backup,MaterialRules.MaterialRuleContext cont) {
            net.minecraft.util.math.random.Random random = Random.create();
            Chunk chunk = cont.chunk;
            HeightContext height = cont.heightContext;
            NoiseConfig noise = cont.noiseConfig;
            int s_m=rock.size();
            double ymax = height.getHeight();
            DoublePerlinNoiseSampler dist_d = noise.getOrCreateSampler(RegistryKey.of(RegistryKeys.NOISE_PARAMETERS, new Identifier(id_d.get(0),id_d.get(1))));
            Float sc_o0=sc_o.get(0);Float sc_o1=sc_o.get(1);Float sc_o2=sc_o.get(2);Float sc_o3=sc_o.get(3);
            Float sc_l0=sc_l.get(0);Float sc_l1=sc_l.get(1);Float sc_l2=sc_l.get(2);Float sc_l3=sc_l.get(3);
            int m0=matrix.get(0);int m1=matrix.get(1);
            float b0=bed.get(0);float b1=bed.get(1);float b2=(bed.get(2)-1)/(m0-1+0F);float b3=(bed.get(3)-1)/(m1-1+0F);
            return (x,y,z)->{
                int ys = (int)(Math.round(backup.ym*0.5 + ymax*0.5));
                double size = Math.pow(sc_l0,sc_l1+sc_l2*backup.dist_os+sc_l3*backup.dist_ls);
                double scale_o=Math.pow(sc_o0,sc_o1+sc_o2*backup.dist_os+sc_o3*backup.dist_ls);
                double dist_od=dist_d.sample(x*scale_o, y*scale_o, z*scale_o);
                double dist_y=Math.pow((ymax-y)/(ymax-b0),b1);
                int layer = (int)(Math.round(abs(y-ys+0.5*backup.point.size()*dist_od)/size)) % backup.point.size();
                int mapy=(int)Math.round(dist_y*(b2*(m0-1)-backup.point.get(layer)%64)+dist_y*(b3*(m1-1)-backup.point.get(layer)/64)*m0);
                BlockPos pos = new BlockPos(x,y,z);
                return rock.get(abs(backup.point.get(layer)+mapy)%s_m).get(random,pos);
            };
        }

        public MaterialRules.BlockStateRule apply(MaterialRules.MaterialRuleContext cont) {
            Chunk chunk = cont.chunk;
            HeightContext height = cont.heightContext;
            NoiseConfig noise = cont.noiseConfig;
            net.minecraft.util.math.random.Random random = Random.create();

            Backup backup = new Backup();

            DoublePerlinNoiseSampler dist_o = noise.getOrCreateSampler(RegistryKey.of(RegistryKeys.NOISE_PARAMETERS, new Identifier(id_o.get(0),id_o.get(1))));
            DoublePerlinNoiseSampler dist_l = noise.getOrCreateSampler(RegistryKey.of(RegistryKeys.NOISE_PARAMETERS, new Identifier(id_l.get(0),id_l.get(1))));
            DoublePerlinNoiseSampler dist_d = noise.getOrCreateSampler(RegistryKey.of(RegistryKeys.NOISE_PARAMETERS, new Identifier(id_d.get(0),id_d.get(1))));

            double ymax = height.getHeight();
            Float xz_o=Float.parseFloat(id_o.get(2));
            Float xz_l=Float.parseFloat(id_l.get(2));
            Float sc_o0=sc_o.get(0);Float sc_o1=sc_o.get(1);Float sc_o2=sc_o.get(2);Float sc_o3=sc_o.get(3);
            Float sc_l0=sc_l.get(0);Float sc_l1=sc_l.get(1);Float sc_l2=sc_l.get(2);Float sc_l3=sc_l.get(3);
            int s_k=goal.size();int s_m=rock.size();
            int m0=matrix.get(0);int m1=matrix.get(1);
            float b0=bed.get(0);float b1=bed.get(1);float b2=(bed.get(2)-1)/(m0-1+0F);float b3=(bed.get(3)-1)/(m1-1+0F);
            return (x,y,z) -> {

                if (x!=backup.xprev||z!=backup.zprev){
                    backup.ym=chunk.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR_WG,x,z);
                    backup.dist_os=(0.5D+0.5D*dist_o.sample(x*xz_o,0, z*xz_o));
                    backup.dist_ls=(0.5D+0.5D*dist_l.sample(x*xz_l,0, z*xz_l));
                    backup.point = new ArrayList<Integer>();
                    for (int k=0; k<s_k;k++) {
                        backup.point.add((int)Math.round(abs((backup.dist_os*(m0-1)+goal.get(k).get(0)+(backup.dist_ls*(m1-1)+goal.get(k).get(1))*m0)))%s_m);
                    }
                }
                int ys = (int)(Math.round(backup.ym*0.5 + ymax*0.5));
                double size = Math.pow(sc_l0,sc_l1+sc_l2*backup.dist_os+sc_l3*backup.dist_ls);
                double scale_o=Math.pow(sc_o0,sc_o1+sc_o2*backup.dist_os+sc_o3*backup.dist_ls);
                double dist_od=dist_d.sample(x*scale_o, y*scale_o, z*scale_o);
                double dist_y=Math.pow((ymax-y)/(ymax-b0),b1);
                int layer = (int)(Math.round(abs(y-ys+0.5*backup.point.size()*dist_od)/size)) % backup.point.size();
                int mapy=(int)Math.round(dist_y*(b2*(m0-1)-backup.point.get(layer)%64)+dist_y*(b3*(m1-1)-backup.point.get(layer)/64)*m0);
                BlockPos pos = new BlockPos(x,y,z);
                backup.xprev=x;
                backup.zprev=z;
                return rock.get(abs(backup.point.get(layer)+mapy)%s_m).get(random,pos);
            };
        }
    }
}