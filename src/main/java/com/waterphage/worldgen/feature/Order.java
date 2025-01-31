package com.waterphage.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class Order extends Multiple {

    public Order(Codec<MultipleConfig> codec) {
        super(codec);
    }

    //Custom feature generation logic that evaluates placement modifiers for each feature.

    private boolean customGenerate(PlacedFeature placedFeature, FeaturePlacementContext context, Random random, BlockPos pos) {
        Stream<BlockPos> positions = Stream.of(pos);

        // Apply all placement modifiers to determine valid positions
        for (PlacementModifier modifier : placedFeature.placementModifiers) {
            positions = positions.flatMap(posx -> modifier.getPositions(context, random, posx));
        }

        // Generate the feature at each determined position
        ConfiguredFeature<?, ?> feature = placedFeature.feature().value();
        MutableBoolean success = new MutableBoolean();
        positions.forEach(placedPos -> {
            if (feature.generate(context.getWorld(), context.getChunkGenerator(), random, placedPos)) {
                success.setTrue();
            }
        });

        return success.isTrue();
    }

    //Generates features sequentially and stops upon the first successful generation.

    @Override
    public boolean generate(FeatureContext<MultipleConfig> context) {
        MultipleConfig config = context.getConfig();
        BlockPos origin = context.getOrigin();
        StructureWorldAccess world = context.getWorld();
        Random random = context.getRandom();
        ChunkGenerator chunkGenerator = context.getGenerator();

        // Generate each feature sequentially
        for (RegistryEntry<PlacedFeature> entry : config.features) {
            PlacedFeature placedFeature = entry.value();
            FeaturePlacementContext placementContext = new FeaturePlacementContext(world, chunkGenerator, Optional.of(placedFeature));

            if (customGenerate(placedFeature, placementContext, random, origin)) {
                return true; // Stop if a feature successfully generates
            }
        }

        return false; // Return false if no feature successfully generated
    }
}
