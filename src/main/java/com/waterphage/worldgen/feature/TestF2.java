package com.waterphage.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.stateprovider.PredicatedStateProvider;

public class TestF2 extends Feature<TestF2.TestF2Config> {
    public TestF2(Codec<TestF2Config> codec) {
        super(codec);
    }

    public boolean generate(FeatureContext<TestF2Config> context) {
        TestF2Config config = context.getConfig();
        BlockPos blockPos = context.getOrigin();
        StructureWorldAccess world = context.getWorld();
        Random random = context.getRandom();

        //String type = String.valueOf(world.getBlockState(blockPos).getBlock().getName());
        //if (type=="minecraft:black_concrete"){}
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        mutable.set(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        if (config.target().test(world, mutable)) {
            BlockState blockState = config.stateProvider().getBlockState(world, random, mutable);
            world.setBlockState(mutable, blockState, 2);
            world.getChunk(mutable).markBlockForPostProcessing(mutable);
        }
        return false;
    }

    public record TestF2Config(PredicatedStateProvider stateProvider, BlockPredicate target) implements FeatureConfig {
        public static final Codec<TestF2Config> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(
                    PredicatedStateProvider.CODEC.fieldOf("state_provider").forGetter(TestF2Config::stateProvider),
                    BlockPredicate.BASE_CODEC.fieldOf("target").forGetter(TestF2Config::target)
            ).apply(instance, TestF2Config::new);
        });
        public TestF2Config(PredicatedStateProvider stateProvider, BlockPredicate target) {
            this.stateProvider = stateProvider;
            this.target = target;
        }
        public PredicatedStateProvider stateProvider() {
            return this.stateProvider;
        }
        public BlockPredicate target() {
            return this.target;
        }
    }
}