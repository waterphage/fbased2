package com.waterphage.worldgen.placers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

import java.util.stream.Stream;

public class Glacier extends PlacementModifier {
    private boolean spacing;
    private int depth;
    public static final Codec<Glacier> MODIFIER_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.BOOL.fieldOf("floor").forGetter(CheckAngle -> CheckAngle.spacing),
                            Codec.INT.fieldOf("depth").forGetter(CheckAngle -> CheckAngle.depth)
                    )
                    .apply(instance, Glacier::new)
    );

    private Glacier(boolean spacing, int depth) {
        this.depth=depth;
        this.spacing = spacing;
    }

    // Factory method to create an instance of BiomeY
    public static Glacier create(boolean spacing, int depth) {
        return new Glacier(spacing,depth);
    }


    @Override
    public Stream<BlockPos> getPositions(FeaturePlacementContext context, Random random, BlockPos pos) {
        StructureWorldAccess world = context.getWorld();
        Stream.Builder<BlockPos> builder = Stream.builder();
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        if (!validateBaseConditions(world, x, y, z)) {
            return Stream.empty();
        }

        for (int i = 1; i <= depth; i++) {
            if (!validateDepthConditions(world, mutablePos, x, y, z, i)) {
                break;
            }

            int ir = (int) Math.round(Math.sqrt(Math.pow(depth, 2) - Math.pow(depth - i, 2)));
            for (int k = 0; k < ir; k++) {
                builder.add(new BlockPos(x, spacing ? y + k + 1 : y - k - 1, z));
            }
        }
        return builder.build();
    }

    private boolean validateBaseConditions(StructureWorldAccess world, int x, int y, int z) {
        return world.getBlockState(new BlockPos(x, y, z)).isSolid()
                && (!spacing ? !world.getBlockState(new BlockPos(x, y - 1, z)).isSolid()
                : world.getBlockState(new BlockPos(x, y + 1, z)).isSolid());
    }

    private boolean validateDepthConditions(StructureWorldAccess world, BlockPos.Mutable mutablePos, int x, int y, int z, int i) {
        // Add depth-specific checks with mutable positions
        mutablePos.set(x, y - (spacing ? i + 3 : -(i + 3)), z);
        return world.getBlockState(mutablePos).isSolid();


    }

    @Override
    public PlacementModifierType<?> getType() {
        return (PlacementModifierType<?>) FbasedPlacers.FBASED_A10;
    }
}