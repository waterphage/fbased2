package com.waterphage.worldgen.rules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.abs;
    public record CheckAngle(List<String> id_o, List<String> id_l, List<String> id_d, List<String> scale, List<CheckAngle.GeoEntry> rock) implements MaterialRules.MaterialRule {
        static final CodecHolder<CheckAngle> CODEC = CodecHolder.of(
                RecordCodecBuilder.mapCodec(
                        instance -> instance.group(
                                        Codec.STRING.listOf().fieldOf("biome_noise_a").forGetter(CheckAngle::id_l),
                                        Codec.STRING.listOf().fieldOf("biome_noise").forGetter(CheckAngle::id_o),
                                        Codec.STRING.listOf().fieldOf("local_noise").forGetter(CheckAngle::id_d),
                                        Codec.STRING.listOf().fieldOf("mode").forGetter(CheckAngle::scale),
                                        CheckAngle.GeoEntry.CODEC.listOf().fieldOf("types").forGetter(CheckAngle::rock)
                                )
                                .apply(instance, CheckAngle::new)
                )
        );

        public static class GeoEntry {
            public static final Codec<CheckAngle.GeoEntry> CODEC = RecordCodecBuilder.create(
                    instance -> instance.group(
                                    Codec.DOUBLE.listOf().fieldOf("c").forGetter(config -> config.coord),
                                    BlockStateProvider.TYPE_CODEC.fieldOf("m").forGetter(config -> config.mineral)
                            )
                            .apply(instance, CheckAngle.GeoEntry::new)
            );
            public final List<Double> coord;
            public final  BlockStateProvider mineral;

            public GeoEntry(List<Double> coord, BlockStateProvider mineral) {
                this.coord = coord;
                this.mineral = mineral;
            }
        }

        public CodecHolder<CheckAngle> Geology(CodecHolder<CheckAngle> codec) {
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

            return backup.stone.get(layer).get(random,pos);
        }
    }
