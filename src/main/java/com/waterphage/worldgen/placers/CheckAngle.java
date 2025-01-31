package com.waterphage.worldgen.placers;

import com.google.gson.Gson;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.waterphage.worldgen.ModRules;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.noise.NoiseRouter;
import net.minecraft.world.gen.placementmodifier.AbstractConditionalPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class CheckAngle extends PlacementModifier {
    private boolean spacing;
    private int depth;
    public static final Codec<CheckAngle> MODIFIER_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.BOOL.fieldOf("floor").forGetter(CheckAngle -> CheckAngle.spacing),
                            Codec.INT.fieldOf("depth").forGetter(CheckAngle -> CheckAngle.depth)
                    )
                    .apply(instance, CheckAngle::new)
    );
    private CheckAngle(boolean spacing, int depth) {
        this.spacing = spacing;
        this.depth = depth;
        this.hash = computeHash();
    }

    private static final Map<Integer, CheckAngle> CACHE = new ConcurrentHashMap<>();
    private final int hash;
    private int computeHash() {return Objects.hash(spacing, depth);}
    public static CheckAngle create(boolean spacing, int depth) {
        return CACHE.computeIfAbsent(new CheckAngle(spacing, depth).hash, hash -> new CheckAngle(spacing, depth));
    }

    private boolean solid(StructureWorldAccess world, Random random, BlockPos pos){
        return true;
    }
    @Override
    public Stream<BlockPos> getPositions(FeaturePlacementContext context, Random random, BlockPos pos) {

        StructureWorldAccess world = context.getWorld();
        Stream.Builder<BlockPos> builder = Stream.builder();
        int x=pos.getX();int y=pos.getY();int z=pos.getZ();
        int m = spacing ? 1:-1;
        BlockPos.Mutable posc = new BlockPos.Mutable(x, y, z);
        if (!world.getBlockState(posc).isSolid()) {return builder.build();}
        if (world.getBlockState(posc.setY(y+m)).isSolid()) {return builder.build();}
        for (int i=1;i<=depth;i++) {
            if (!world.getBlockState(posc.set(x, y - m * (i + 3), z)).isSolid()) {return builder.build();}
            if (!world.getBlockState(posc.set(x -i-1, y -m*(i+1), z -i-1)).isSolid()) {return builder.build();}
            if (!world.getBlockState(posc.setZ(z +i+1)).isSolid()) {return builder.build();}
            if (!world.getBlockState(posc.setX(x +i+1)).isSolid()) {return builder.build();}
            if (!world.getBlockState(posc.setZ(z -i-1)).isSolid()) {return builder.build();}
            if (world.getBlockState(posc.setY(y + m*(i+2))).isSolid()) {return builder.build();}
            if (world.getBlockState(posc.setZ(z +i+1)).isSolid()) {return builder.build();}
            if (world.getBlockState(posc.setX(x -i-1)).isSolid()) {return builder.build();}
            if (world.getBlockState(posc.setZ(z -i-1)).isSolid()) {return builder.build();}
            builder.add(new BlockPos(x, y-m*i, z));
        }
        return builder.build();
    }
    @Override
    public PlacementModifierType<?> getType() {
        return (PlacementModifierType<?>) FbasedPlacers.FBASED_A5;
    }
}