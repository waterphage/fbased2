package com.waterphage.worldgen.placers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

import java.util.stream.Stream;

public class RandomS extends PlacementModifier {

    public static final Codec<RandomS> MODIFIER_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.floatRange(0.0F, 1.0F).fieldOf("pass").forGetter(placer -> placer.pass)
            ).apply(instance, RandomS::new)
    );

    private final float pass;

    private RandomS(float pass) {
        this.pass = pass;
    }

    public static RandomS of(float pass) {
        return new RandomS(pass);
    }

    @Override
    public Stream<BlockPos> getPositions(FeaturePlacementContext context, Random random, BlockPos pos) {
        return random.nextFloat() < this.pass
                ? Stream.of(pos)
                : Stream.empty();
    }

    @Override
    public PlacementModifierType<?> getType() {
        return (PlacementModifierType<?>) FbasedPlacers.FBASED_A12;
    }
}