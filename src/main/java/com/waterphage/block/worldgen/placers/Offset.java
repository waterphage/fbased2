package com.waterphage.block.worldgen.placers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

import java.util.List;
import java.util.stream.Stream;

import static java.lang.Math.abs;

public class Offset extends PlacementModifier {

    public static final Codec<Offset> MODIFIER_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.INT.listOf().fieldOf("xyz").forGetter(Layer -> Layer.xyz)
                    )
                    .apply(instance, Offset::new)
    );
    private final List<Integer> xyz;

    private Offset(List<Integer> xyz) {
        this.xyz = xyz;
    }


    public static Offset of(List<Integer> xyz) {
        return new Offset(xyz);
    }
    @Override
    public Stream<BlockPos> getPositions(FeaturePlacementContext context, Random random, BlockPos pos) {
        Stream.Builder<BlockPos> builder = Stream.builder();
        builder.add(new BlockPos(pos.getX()+xyz.get(0),pos.getY()+xyz.get(1),pos.getZ()+xyz.get(2)));
        return builder.build();
    }

    @Override
    public PlacementModifierType<?> getType() {
        return (PlacementModifierType<?>) FbasedPlacers.FBASED_A7;
    }
}
