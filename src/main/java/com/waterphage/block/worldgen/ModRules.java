package com.waterphage.block.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.waterphage.Fbased;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;

import static java.lang.Math.abs;

public class ModRules extends MaterialRules {
    public static void registerrules() {
        Registry.register(Registries.MATERIAL_RULE, new Identifier(Fbased.MOD_ID, "geology"), Geology.CODEC.codec());
    }

    static <A> Codec<? extends A> register(Registry<Codec<? extends A>> registry, String id, CodecHolder<? extends A> codecHolder) {
        return Registry.register(registry, new Identifier(Fbased.MOD_ID, id), codecHolder.codec());
    }

    public interface MaterialRule extends Function<MaterialRuleContext, BlockStateRule> {
        Codec<MaterialRules.MaterialRule> CODEC = Registries.MATERIAL_RULE.getCodec().dispatch(materialRule -> materialRule.codec().codec(), Function.identity());

        static Codec<? extends MaterialRules.MaterialRule> registerAndGetDefault(Registry<Codec<? extends MaterialRules.MaterialRule>> registry) {
            return ModRules.register(registry, "geology", ModRules.Geology.CODEC);
        }

        CodecHolder<? extends MaterialRules.MaterialRule> codec();
    }

    public static ModRules.Geology condition(int ifTrue, List<BlockStateProvider> rock) {
        return new ModRules.Geology(ifTrue, rock);
    }

    record Geology(int ifTrue, List<BlockStateProvider> rock) implements MaterialRules.MaterialRule {
        static final CodecHolder<ModRules.Geology> CODEC = CodecHolder.of(
                RecordCodecBuilder.mapCodec(
                        instance -> instance.group(
                                        Codec.INT.fieldOf("a").forGetter(ModRules.Geology::ifTrue),
                                        BlockStateProvider.TYPE_CODEC.listOf().fieldOf("types").forGetter(ModRules.Geology::rock)
                                )
                                .apply(instance, ModRules.Geology::new)
                )
        );

        public static class MaterialRuleContextUtil {

            public static SurfaceBuilder getSurfaceBuilder(Object context) {
                try {
                    Field field = context.getClass().getDeclaredField("surfaceBuilder");
                    field.setAccessible(true);
                    return (SurfaceBuilder) field.get(context);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException("Failed to access field 'surfaceBuilder'", e);
                }
            }

            public static NoiseConfig getNoiseConfig(Object context) {
                try {
                    Field field = context.getClass().getDeclaredField("noiseConfig");
                    field.setAccessible(true);
                    return (NoiseConfig) field.get(context);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException("Failed to access field 'noiseConfig'", e);
                }
            }

            public static Chunk getChunk(Object context) {
                try {
                    Field field = context.getClass().getDeclaredField("chunk");
                    field.setAccessible(true);
                    return (Chunk) field.get(context);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException("Failed to access field 'chunk'", e);
                }
            }

            public static ChunkNoiseSampler getChunkNoiseSampler(Object context) {
                try {
                    Field field = context.getClass().getDeclaredField("chunkNoiseSampler");
                    field.setAccessible(true);
                    return (ChunkNoiseSampler) field.get(context);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException("Failed to access field 'chunkNoiseSampler'", e);
                }
            }

            public static Function<BlockPos, RegistryEntry<Biome>> getPosToBiome(Object context) {
                try {
                    Field field = context.getClass().getDeclaredField("posToBiome");
                    field.setAccessible(true);
                    return (Function<BlockPos, RegistryEntry<Biome>>) field.get(context);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException("Failed to access field 'posToBiome'", e);
                }
            }

            public static HeightContext getHeightContext(Object context) {
                try {
                    Field field = context.getClass().getDeclaredField("heightContext");
                    field.setAccessible(true);
                    return (HeightContext) field.get(context);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException("Failed to access field 'heightContext'", e);
                }
            }

            // Additional getters as needed
        }

        public CodecHolder<Geology> Geology(CodecHolder<Geology> codec) {
            return CODEC;
        }

        @Override
        public CodecHolder<? extends MaterialRules.MaterialRule> codec() {
            return CODEC;
        }

        public MaterialRules.BlockStateRule apply(MaterialRules.MaterialRuleContext cont) {
            SurfaceBuilder surf = MaterialRuleContextUtil.getSurfaceBuilder(cont);
            Chunk chunk = MaterialRuleContextUtil.getChunk(cont);
            ChunkNoiseSampler sampl = MaterialRuleContextUtil.getChunkNoiseSampler(cont);
            HeightContext height = MaterialRuleContextUtil.getHeightContext(cont);
            NoiseConfig noise = MaterialRuleContextUtil.getNoiseConfig(cont);
            Function<BlockPos, RegistryEntry<Biome>> biome = MaterialRuleContextUtil.getPosToBiome(cont);
            net.minecraft.util.math.random.Random random = Random.create();
            return (x, y, z) -> {

                int ymax = height.getHeight();
                int ym = chunk.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR_WG,x,z);
                int ys = (ym + ymax) / 2;
                int layer = abs(y - ys) % rock.size();
                BlockPos pos = new BlockPos(x,y,z);
                return rock.get(layer).get(random,pos);
            };
        }
    }
}
