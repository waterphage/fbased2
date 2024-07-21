package com.waterphage.block.worldgen.placers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.placementmodifier.AbstractConditionalPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

public class BiomeY extends AbstractConditionalPlacementModifier {
    private int spacing;

    public static final Codec<BiomeY> MODIFIER_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.INT.fieldOf("y").forGetter(geoPlacerSurf -> geoPlacerSurf.spacing)
                    )
                    .apply(instance, BiomeY::new)
    );

    private BiomeY(int spacing) {
        this.spacing = spacing;
    }

    // Factory method to create an instance of BiomeY
    public static BiomeY create(int spacing) {
        return new BiomeY(spacing);
    }

    @Override
    protected boolean shouldPlace(FeaturePlacementContext context, Random random, BlockPos pos) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        mutable.set(pos.getX(), spacing, pos.getZ());
        PlacedFeature placedFeature = (PlacedFeature)context.getPlacedFeature()
                .orElseThrow(() -> new IllegalStateException("Tried to biome check an unregistered feature, or a feature that should not restrict the biome"));
        RegistryEntry<Biome> registryEntry = context.getWorld().getBiome(mutable);
        //boolean l = 0.5>sin(2*context.getHeight()/2*(pos.getY()-context.getBottomY())/(context.getHeight()+context.getTopY(Heightmap.Type.WORLD_SURFACE_WG, pos.getX(), pos.getZ())-context.getBottomY()));
        return context.getChunkGenerator().getGenerationSettings(registryEntry).isFeatureAllowed(placedFeature);
    }

    @Override
    public PlacementModifierType<?> getType() {
        return (PlacementModifierType<?>) FbasedPlacers.FBASED_A2;
    }
}