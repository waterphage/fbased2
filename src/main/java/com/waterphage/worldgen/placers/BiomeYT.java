package com.waterphage.worldgen.placers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.placementmodifier.AbstractConditionalPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

public class BiomeYT extends AbstractConditionalPlacementModifier {
    private int spacing;

    public static final Codec<BiomeYT> MODIFIER_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.INT.fieldOf("y").forGetter(geoPlacerSurf -> geoPlacerSurf.spacing)
                    )
                    .apply(instance, BiomeYT::new)
    );

    private BiomeYT(int spacing) {
        this.spacing = spacing;
    }
    private Identifier goal=new Identifier("fbased","main");

    // Factory method to create an instance of BiomeY
    public static BiomeYT create(int spacing) {
        return new BiomeYT(spacing);
    }

    @Override
    protected boolean shouldPlace(FeaturePlacementContext context, Random random, BlockPos pos) {
        BlockPos.Mutable mutable = new BlockPos.Mutable(pos.getX(), spacing, pos.getZ());
        RegistryEntry<Biome> registryEntry = context.getWorld().getBiome(mutable);
        return (registryEntry.matchesId(goal));
    }

    @Override
    public PlacementModifierType<?> getType() {
        return (PlacementModifierType<?>) FbasedPlacers.FBASED_A11;
    }
}