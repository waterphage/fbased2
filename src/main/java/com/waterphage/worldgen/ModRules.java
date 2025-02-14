package com.waterphage.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.waterphage.Fbased;
import com.waterphage.block.models.TechBlock;
import com.waterphage.block.models.TechBlockEntity;
import com.waterphage.meta.IntPair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctions;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.noise.NoiseRouter;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import org.spongepowered.asm.mixin.injection.struct.InjectorGroupInfo;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.lang.Math.abs;

public class ModRules extends MaterialRules {
    public static void registerrules() {
        Registry.register(Registries.MATERIAL_RULE, new Identifier(Fbased.MOD_ID, "geology_d"), GeologyD.CODEC.codec());
        Registry.register(Registries.MATERIAL_RULE, new Identifier(Fbased.MOD_ID, "cache"), Cache.CODEC.codec());
    }

    static <A> Codec<? extends A> register(Registry<Codec<? extends A>> registry, String id, CodecHolder<? extends A> codecHolder) {
        return Registry.register(registry, new Identifier(Fbased.MOD_ID, id), codecHolder.codec());
    }

    public interface MaterialRule extends Function<MaterialRuleContext, BlockStateRule> {
        Codec<MaterialRules.MaterialRule> CODEC = Registries.MATERIAL_RULE.getCodec().dispatch(materialRule -> materialRule.codec().codec(), Function.identity());

        static Codec<? extends MaterialRules.MaterialRule> registerAndGetDefault(Registry<Codec<? extends MaterialRules.MaterialRule>> registry) {
            ModRules.register(registry, "cache", ModRules.Cache.CODEC);
            return ModRules.register(registry, "geology_d", ModRules.GeologyD.CODEC);
        }

        CodecHolder<? extends MaterialRules.MaterialRule> codec();
    }

    public static ModRules.Cache condition(Integer yT,BlockStateProvider fallback) {
        return new ModRules.Cache(yT);
    }

    record Cache(Integer yT) implements MaterialRules.MaterialRule {

        // Codec for serializing and deserializing the rule
        static final CodecHolder<ModRules.Cache> CODEC = CodecHolder.of(
                RecordCodecBuilder.mapCodec(
                        instance -> instance.group(
                                        Codec.INT.fieldOf("tech_Y").forGetter(ModRules.Cache::yT)
                                )
                                .apply(instance, ModRules.Cache::new)
                )
        );

        @Override
        public CodecHolder<? extends MaterialRules.MaterialRule> codec() {
            return CODEC;
        }

        private void runy(BlockState key,int miny,int maxy,int x, int z, Chunk chunk){
            BlockPos.Mutable pos=new BlockPos.Mutable(x,0,z);
            int i=yT;
            for (int y=miny;y<=maxy;y++){
                pos.setY(y);
                if(chunk.getBlockState(pos).isSolid()){
                    if(!chunk.getBlockState(pos.setY(y+1)).isSolid()&&chunk.getBlockState(pos.setY(y-3)).isSolid()){
                        chunk.setBlockState(pos.setY(i),key.with(TechBlock.CACHE,y),false);y+=1;
                        chunk.setBlockState(pos.setY(i+1),key.with(TechBlock.CACHE,34),false);y+=1;
                        i+=2;
                    }
                    if(!chunk.getBlockState(pos.setY(y-1)).isSolid()&&chunk.getBlockState(pos.setY(y+3)).isSolid()){
                        chunk.setBlockState(pos.setY(i),key.with(TechBlock.CACHE,y),false);y+=1;
                        chunk.setBlockState(pos.setY(i+1),key.with(TechBlock.CACHE,32),false);y+=1;
                        i+=2;
                    }
                }
            }
        }
        public MaterialRules.BlockStateRule apply(MaterialRules.MaterialRuleContext context) {
            Chunk chunk = context.chunk;
            ChunkPos or = chunk.getPos();
            int miny=chunk.getBottomY()+2;
            for (int x=0;x<=15;x++){
                for (int z=0;z<=15;z++){
                    int maxy=chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG).get(x,z);
                    BlockState key = Registries.BLOCK.get(new Identifier("fbased:surface_cache")).getDefaultState();
                    runy(key,miny,maxy,x, z,chunk);
                }
            }
            return (x, y, z) -> {return chunk.getBlockState(new BlockPos(x,y,z));};
        }
    }

    // Function to create a new GeologyD rule
// This is a factory method to simplify creating instances of the GeologyD class
    public static ModRules.GeologyD condition(String idL, String idO, Integer yT, List<String> idD,
                                              List<Float> scaleOffsets, List<Float> scaleWidths,
                                              List<Integer> matrix, List<List<Integer>> goal,
                                              List<Float> bedrockParams, List<BlockStateProvider> rockTypes) {
        return new ModRules.GeologyD(idL, idO, yT, idD, scaleOffsets, scaleWidths, matrix, goal, bedrockParams, rockTypes);
    }

    // Enum to map different noise types to their respective functions in the NoiseRouter
    private enum NoiseType {
        TEMP {@Override public DensityFunction getNoise(NoiseRouter params) {
                return params.temperature();
            }},
        CONT {@Override public DensityFunction getNoise(NoiseRouter params) {
                return params.continents();
            }},
        WERD {@Override public DensityFunction getNoise(NoiseRouter params) {
                return params.ridges();
            }},
        HUM {@Override public DensityFunction getNoise(NoiseRouter params) {
                return params.vegetation();
            }},
        EROS {@Override public DensityFunction getNoise(NoiseRouter params) {
                return params.erosion();
            }},
        DEPT {@Override public DensityFunction getNoise(NoiseRouter params) {
                return params.depth();
            }},
        INIT {@Override public DensityFunction getNoise(NoiseRouter params) {return params.initialDensityWithoutJaggedness();}},
        LAVA {@Override public DensityFunction getNoise(NoiseRouter params) {return params.lavaNoise();}},
        SPRD {@Override public DensityFunction getNoise(NoiseRouter params) {
                return params.fluidLevelSpreadNoise();
            }},
        FLOD {@Override public DensityFunction getNoise(NoiseRouter params) {return params.fluidLevelFloodednessNoise();}},
        BARR {@Override public DensityFunction getNoise(NoiseRouter params) {return params.barrierNoise();}},
        VRID {@Override public DensityFunction getNoise(NoiseRouter params) {return params.veinRidged();}},
        VGAP {@Override public DensityFunction getNoise(NoiseRouter params) {return params.veinGap();}},
        VTOG {@Override public DensityFunction getNoise(NoiseRouter params) {return params.veinToggle();}},
        FIN {@Override public DensityFunction getNoise(NoiseRouter params) {return params.finalDensity();}};

        // Abstract method to be implemented by all noise types
        public abstract DensityFunction getNoise(NoiseRouter params);
    }

    // Helper class to store and manage intermediate state for block generation
    private static class Backup {
        private int xPrev = -9999;
        private int zPrev = -9999;
        private int yMax;
        private List<Integer> points = new ArrayList<>();
        private NoiseType biomeX;
        private NoiseType biomeZ;
        private double distOs;
        private double distLs;

        // Check if the current position has changed
        public boolean isPositionChanged(int x, int z) {
            return x != xPrev || z != zPrev;
        }

        // Update backup state with new values
        public void updatePosition(int x, int z, int yMax, double distOs, double distLs, List<Integer> points) {
            this.xPrev = x;
            this.zPrev = z;
            this.yMax = yMax;
            this.distOs = distOs;
            this.distLs = distLs;
            this.points = points;
        }

        public int getXPrev() {
            return xPrev;
        }

        public int getZPrev() {
            return zPrev;
        }
    }

    // Map a string to its corresponding NoiseType enum value
    private static NoiseType fromString(String name) {
        return switch (name.toLowerCase()) {
            case "temperature" -> NoiseType.TEMP;
            case "humidity" -> NoiseType.HUM;
            case "continentalness" -> NoiseType.CONT;
            case "erosion" -> NoiseType.EROS;
            case "depth" -> NoiseType.DEPT;
            case "ridges" -> NoiseType.WERD;
            case "barrier" -> NoiseType.BARR;
            case "fluid_level_floodedness" -> NoiseType.FLOD;
            case "fluid_level_spread" -> NoiseType.SPRD;
            case "lava" -> NoiseType.LAVA;
            case "initial_density_without_jaggedness" -> NoiseType.INIT;
            case "final_density" -> NoiseType.FIN;
            case "vein_ridged" -> NoiseType.VRID;
            case "vein_toggle" -> NoiseType.VTOG;
            case "vein_gap" -> NoiseType.VGAP;
            default -> throw new IllegalArgumentException("Unknown noise type: " + name);
        };
    }

    // Wraps a coordinate into a specific range, ensuring values stay within bounds
    private static int wrapCoordinate(int coord, int size) {
        if (coord < 0) return -coord - 1;
        if (coord >= size) return size - (coord - size + 1);
        return coord;
    }

    // Main record defining the GeologyD terrain rule
    record GeologyD(String idL, String idO, Integer yT, List<String> idD, List<Float> scaleOffsets,
                    List<Float> scaleWidths, List<Integer> matrix, List<List<Integer>> goal,
                    List<Float> bedrockParams, List<BlockStateProvider> rockTypes) implements MaterialRules.MaterialRule {

        // Codec for serializing and deserializing the rule
        static final CodecHolder<ModRules.GeologyD> CODEC = CodecHolder.of(
                RecordCodecBuilder.mapCodec(
                        instance -> instance.group(
                                        Codec.STRING.fieldOf("biome_noise_z").forGetter(ModRules.GeologyD::idL),
                                        Codec.STRING.fieldOf("biome_noise_x").forGetter(ModRules.GeologyD::idO),
                                        Codec.INT.fieldOf("tech_Y").forGetter(ModRules.GeologyD::yT),
                                        Codec.STRING.listOf().fieldOf("local_noise").forGetter(ModRules.GeologyD::idD),
                                        Codec.FLOAT.listOf().fieldOf("offset").forGetter(ModRules.GeologyD::scaleOffsets),
                                        Codec.FLOAT.listOf().fieldOf("width").forGetter(ModRules.GeologyD::scaleWidths),
                                        Codec.INT.listOf().fieldOf("matrix").forGetter(ModRules.GeologyD::matrix),
                                        Codec.INT.listOf().listOf().fieldOf("goal").forGetter(ModRules.GeologyD::goal),
                                        Codec.FLOAT.listOf().fieldOf("bedrock").forGetter(ModRules.GeologyD::bedrockParams),
                                        BlockStateProvider.TYPE_CODEC.listOf().fieldOf("types").forGetter(ModRules.GeologyD::rockTypes)
                                )
                                .apply(instance, ModRules.GeologyD::new)
                )
        );

        @Override
        public CodecHolder<? extends MaterialRules.MaterialRule> codec() {
            return CODEC;
        }

        // Main method for applying the terrain rule
        public MaterialRules.BlockStateRule apply(MaterialRules.MaterialRuleContext context) {
            Chunk chunk = context.chunk;
            HeightContext height = context.heightContext;
            NoiseConfig noise = context.noiseConfig;
            net.minecraft.util.math.random.Random random = Random.create();
            Backup backup = new Backup();
            backup.biomeX = fromString(idO);
            backup.biomeZ = fromString(idL);

            if (idD.size() < 2) {
                throw new IllegalArgumentException("idD must have at least two elements for namespace and path.");
            }
            DoublePerlinNoiseSampler distSampler = noise.getOrCreateSampler(
                    RegistryKey.of(RegistryKeys.NOISE_PARAMETERS, new Identifier(idD.get(0), idD.get(1)))
            );

            double yMax = height.getHeight();
            Float scaleOffset0 = scaleOffsets.get(0), scaleOffset1 = scaleOffsets.get(1);
            Float scaleOffset2 = scaleOffsets.get(2), scaleOffset3 = scaleOffsets.get(3);
            Float scaleWidth0 = scaleWidths.get(0), scaleWidth1 = scaleWidths.get(1);
            Float scaleWidth2 = scaleWidths.get(2), scaleWidth3 = scaleWidths.get(3);
            int goalSize = goal.size(), rockSize = rockTypes.size();
            int matrixX = matrix.get(0), matrixY = matrix.get(1);
            float bedrockStart = bedrockParams.get(0), bedrockGradient = bedrockParams.get(1);
            int bedrockBase = bedrockParams.get(2).intValue(), bedrockHeight = bedrockParams.get(3).intValue();
            return (x, y, z) -> {

                if (backup.isPositionChanged(x, z)) {
                    DensityFunction.NoisePos pos = new DensityFunction.NoisePos() {
                        @Override public int blockX() { return x; }
                        @Override public int blockY() { return yT; }
                        @Override public int blockZ() { return z; }
                    };

                    double newDistOs = backup.biomeX.getNoise(noise.getNoiseRouter()).sample(pos);
                    double newDistLs = backup.biomeZ.getNoise(noise.getNoiseRouter()).sample(pos);
                    List<Integer> newPoints = calculatePoints(goal, matrixX, matrixY, newDistOs, newDistLs);

                    backup.updatePosition(
                            x, z,
                            chunk.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR_WG, x, z),
                            newDistOs,
                            newDistLs,
                            newPoints
                    );
                }

                int adjustedY = (int) Math.round(backup.yMax * 0.5 + yMax * 0.5);
                double size = Math.pow(scaleWidth0, scaleWidth1 + scaleWidth2 * backup.distOs + scaleWidth3 * backup.distLs);
                double offsetScale = Math.pow(scaleOffset0, scaleOffset1 + scaleOffset2 * backup.distOs + scaleOffset3 * backup.distLs);
                double distOffset = distSampler.sample(x * offsetScale, y * offsetScale, z * offsetScale);
                double distY = Math.pow((yMax - y - 0.5 * ((backup.points.size() * distOffset) % backup.points.size())) / (yMax - bedrockStart), bedrockGradient);

                int layer = (int) (Math.round(Math.abs(y - adjustedY + 0.5 * backup.points.size() * distOffset) / size)) % backup.points.size();
                int mx = backup.points.get(layer) % matrixX;
                int my = backup.points.get(layer) / matrixX;

                mx = (int) MathHelper.clamp(distY * bedrockBase + (1.0 - distY) * mx, 0, matrixX - 1);
                my = (int) MathHelper.clamp(distY * bedrockHeight + (1.0 - distY) * my, 0, matrixY - 1);

                BlockPos pos = new BlockPos(x, y, z);
                return rockTypes.get(mx + my * matrixX).get(random, pos);
            };
        }

        private List<Integer> calculatePoints(List<List<Integer>> goal, int matrixX, int matrixY, double distOs, double distLs) {
            List<Integer> points = new ArrayList<>();
            int mx = (int) MathHelper.clamp((distOs + 1.0) / 2.0 * matrixX - 1, 0, matrixX - 1);
            int my = (int) MathHelper.clamp((distLs + 1.0) / 2.0 * matrixY - 1, 0, matrixY - 1);

            for (List<Integer> offset : goal) {
                mx = wrapCoordinate(mx + offset.get(0), matrixX);
                my = wrapCoordinate(my + offset.get(1), matrixY);
                points.add(mx + my * matrixX);
            }
            return points;
        }
    }


}