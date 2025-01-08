package com.waterphage.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.stateprovider.PredicatedStateProvider;

public class TestF extends Feature<TestF.TestFConfig> {
    public TestF(Codec<TestFConfig> codec) {
        super(codec);
    }

    public boolean generate(FeatureContext<TestFConfig> context) {
        TestFConfig config = context.getConfig();
        BlockPos blockPos = context.getOrigin();
        StructureWorldAccess world = context.getWorld();
        Random random = context.getRandom();

        String type = String.valueOf(world.getBlockState(blockPos).getBlock().getName());
        if (type=="minecraft:black_concrete"){}

        int Ymax = world.getBottomY();
        for (int j = 0; j < 16; ++j) {
            int lx = blockPos.getX()+j;
            for (int k = 0; k < 16; ++k) {
                int lz = blockPos.getZ()+k;
                Ymax = Math.max(Ymax,world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, lx, lz));
            }
        }

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int i = Ymax; i > world.getBottomY(); --i) {
            for (int j = 0; j < 16; ++j) {
                int x = blockPos.getX()+j;
                for (int k = 0; k < 16; ++k) {
                    int z = blockPos.getZ()+k;
                    mutable.set(x, i, z);
                    if (config.target().test(world, mutable)) {
                        BlockState blockState = config.stateProvider().getBlockState(world, random, mutable);
                        world.setBlockState(mutable, blockState, 2);
                        world.getChunk(mutable).markBlockForPostProcessing(mutable);
                    }
                }
            }
        }
        return false;
    }

    public record TestFConfig(PredicatedStateProvider stateProvider, BlockPredicate target) implements FeatureConfig {
        public static final Codec<TestFConfig> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(
                    PredicatedStateProvider.CODEC.fieldOf("state_provider").forGetter(TestFConfig::stateProvider),
                    BlockPredicate.BASE_CODEC.fieldOf("target").forGetter(TestFConfig::target)
            ).apply(instance, TestFConfig::new);
        });
        public TestFConfig(PredicatedStateProvider stateProvider, BlockPredicate target) {
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
