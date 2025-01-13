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

    private boolean customGenerate(PlacedFeature placedFeature, FeaturePlacementContext context, Random random, BlockPos pos) {
        Stream<BlockPos> stream = Stream.of(pos);
        for (PlacementModifier placementModifier : placedFeature.placementModifiers) {
            stream = stream.flatMap(posx -> placementModifier.getPositions(context, random, posx));
        }
        ConfiguredFeature<?, ?> configuredFeature = placedFeature.feature().value();

        MutableBoolean success = new MutableBoolean();
        stream.forEach(placedPos -> {
            if (configuredFeature.generate(context.getWorld(), context.getChunkGenerator(), random, placedPos)) {
                success.setTrue();
            }
        });
        return success.isTrue();
    }
    @Override
    public boolean generate(FeatureContext<MultipleConfig> context) {
        MultipleConfig config = context.getConfig();
        BlockPos pos = context.getOrigin();
        StructureWorldAccess world = context.getWorld();
        Random random = context.getRandom();
        ChunkGenerator chunkGenerator = context.getGenerator();

        for (RegistryEntry<PlacedFeature> entry : config.features) {
            PlacedFeature pl = entry.value();
            FeaturePlacementContext fcont = new FeaturePlacementContext(world, chunkGenerator, Optional.ofNullable(pl));
            if (customGenerate(pl, fcont, random, pos)){return true;}
        }
        return false;
    }
}