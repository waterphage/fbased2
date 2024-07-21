package com.waterphage.block.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.stateprovider.PredicatedStateProvider;

public class Disk extends Feature<Disk.DiskConfig> {
    public Disk(Codec<DiskConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DiskConfig> context) {
        DiskConfig diskConfig = context.getConfig();
        BlockPos blockPos = context.getOrigin();
        StructureWorldAccess structureWorldAccess = context.getWorld();
        Random random = context.getRandom();
        boolean bl = false;
        int i = blockPos.getY();
        int j = i + diskConfig.halfHeight();
        int k = i - diskConfig.halfHeight() - 1;
        int l = diskConfig.radius().get(random);
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for(BlockPos blockPos2 : BlockPos.iterate(blockPos.add(-l, 0, -l), blockPos.add(l, 0, l))) {
            int m = blockPos2.getX() - blockPos.getX();
            int n = blockPos2.getZ() - blockPos.getZ();
            if (m * m + n * n <= l * l) {
                bl |= this.placeBlock(diskConfig, structureWorldAccess, random, j, k, mutable.set(blockPos2));
            }
        }

        return bl;
    }

    protected boolean placeBlock(DiskConfig config, StructureWorldAccess world, Random random, int topY, int bottomY, BlockPos.Mutable pos) {
        boolean bl = false;

        for(int i = topY; i > bottomY; --i) {
            pos.setY(i);
            if (config.target().test(world, pos)) {
                BlockState blockState = config.stateProvider().getBlockState(world, random, pos);
                world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS);
                this.markBlocksAboveForPostProcessing(world, pos);
                bl = true;
            }
        }

        return bl;
    }

    public static record DiskConfig(PredicatedStateProvider stateProvider, BlockPredicate target, IntProvider radius, int halfHeight) implements FeatureConfig {
        public static final Codec<DiskConfig> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                PredicatedStateProvider.CODEC.fieldOf("state_provider").forGetter(DiskConfig::stateProvider),
                                BlockPredicate.BASE_CODEC.fieldOf("target").forGetter(DiskConfig::target),
                                IntProvider.createValidatingCodec(0, 9999).fieldOf("radius").forGetter(DiskConfig::radius),
                                Codec.intRange(0, 4).fieldOf("half_height").forGetter(DiskConfig::halfHeight)
                        )
                        .apply(instance, DiskConfig::new)
        );
    }
}
