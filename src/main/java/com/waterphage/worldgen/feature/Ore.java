package com.waterphage.worldgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.SimpleBlockFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Ore extends Feature<Ore.OreConfig> {
    public Ore(Codec<Ore.OreConfig> codec) {
        super(codec);
    }

    public static record OreConfig(List<OreConfig.Target> targets) implements FeatureConfig {
        public static final Codec<OreConfig> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(Codec.list(OreConfig.Target.CODEC).fieldOf("targets").forGetter((config) -> {
                return config.targets;
            })).apply(instance, OreConfig::new);
        });

        public OreConfig(List<OreConfig.Target> targets) {
            this.targets = targets;
        }

        public static OreConfig.Target createTarget(RuleTest test, BlockState state) {
            return new OreConfig.Target(test, state);
        }

        private static class Target {
            public static final Codec<OreConfig.Target> CODEC = RecordCodecBuilder.create((instance) -> {
                return instance.group(RuleTest.TYPE_CODEC.fieldOf("target").forGetter((target) -> {
                    return target.target;
                }), BlockState.CODEC.fieldOf("state").forGetter((target) -> {
                    return target.state;
                })).apply(instance, OreConfig.Target::new);
            });
            public final RuleTest target;
            public final BlockState state;

            Target(RuleTest target, BlockState state) {
                this.target = target;
                this.state = state;
            }
        }
    }

    public boolean generate(FeatureContext<OreConfig> context) {
        Random random = context.getRandom();
        OreConfig ore = context.getConfig();
        StructureWorldAccess world = context.getWorld();
        BlockPos pos = context.getOrigin();
        Iterator test = ore.targets.iterator();
        BlockState blc = world.getBlockState(pos);
        while (test.hasNext()) {
            OreConfig.Target target = (OreConfig.Target) test.next();
            if (target.target.test(blc, random)) {
                BlockState block = target.state;
                if (block.canPlaceAt(world, pos)) {
                    if (block.getBlock() instanceof TallPlantBlock) {
                        if (!world.isAir(pos.up())) {
                            return false;
                        }
                        TallPlantBlock.placeAt(world, block, pos, 2);
                    } else {
                        world.setBlockState(pos, block, 2);
                    }
                    return true;
                }
                return false;
            }
        }
        return false;
    }
}
