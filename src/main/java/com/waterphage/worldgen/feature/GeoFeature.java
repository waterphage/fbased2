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

public class GeoFeature extends Feature<GeoFeature.GeoFeatureConfig> {
    public GeoFeature(Codec<GeoFeatureConfig> codec) {
        super(codec);
    }

    public boolean generate(FeatureContext<GeoFeatureConfig> context) {
        GeoFeatureConfig geoconfig = context.getConfig();
        BlockPos blockPos = context.getOrigin();
        StructureWorldAccess structureWorldAccess = context.getWorld();
        Random random = context.getRandom();
        boolean bl = false;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        bl |= this.placeBlock(geoconfig, structureWorldAccess, random, mutable.set(blockPos));
        return bl;
    }
        protected boolean placeBlock(GeoFeatureConfig config, StructureWorldAccess world, Random random, BlockPos.Mutable pos) {
            int Ymax=world.getBottomY();
            for (int j = 0; j < 16; ++j) {
                int lx=pos.getX() + j + 1;
                for (int k = 0; k < 16; ++k) {
                    int lz=pos.getZ() - k - 1;
                    int Yloc=world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG,lx,lz);
                    if (Yloc>Ymax){Ymax=Yloc;}
                }
            }
            int xo=pos.getX();
            int zo=pos.getZ();
            for (int i = Ymax; i > world.getBottomY(); --i) {
                pos.setY(i);
                for (int j = 0; j < 16; ++j) {
                    pos.setX(xo+j+1);
                    for (int k = 0; k < 16; ++k) {
                        pos.setZ(zo-k-1);
                        if (config.target().test(world, pos)) {
                            BlockState blockState = config.stateProvider().getBlockState(world, random, pos);
                            world.setBlockState(pos, blockState, 2);
                            this.markBlocksAboveForPostProcessing(world, pos);
                        }
                    }
                }
            }
            return false;
        }

    public static record GeoFeatureConfig(PredicatedStateProvider stateProvider, BlockPredicate target) implements FeatureConfig {
        public static final Codec<GeoFeatureConfig> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(
                    PredicatedStateProvider.CODEC.fieldOf("state_provider").forGetter(GeoFeatureConfig::stateProvider),
                    BlockPredicate.BASE_CODEC.fieldOf("target").forGetter(GeoFeatureConfig::target)
            ).apply(instance, GeoFeatureConfig::new);
        });
        public GeoFeatureConfig(PredicatedStateProvider stateProvider, BlockPredicate target) {
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
