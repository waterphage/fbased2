package com.waterphage.worldgen.placers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.waterphage.Fbased;
import com.waterphage.worldgen.feature.BiomeF;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.placementmodifier.AbstractConditionalPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

import java.util.List;
import java.util.Optional;

public class BiomeY extends AbstractConditionalPlacementModifier {
    private Optional<Integer> spacing;
    private Optional<List<Identifier>> biomet;
    private Optional<List<Identifier>> biomei;
    private Optional<List<Identifier>> biomes;

    public static final Codec<BiomeY> MODIFIER_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.optionalFieldOf("y").forGetter(geoPlacerSurf -> geoPlacerSurf.spacing),
                    Identifier.CODEC.listOf().optionalFieldOf("tag").forGetter(geoPlacerSurf -> geoPlacerSurf.biomet),
                    Identifier.CODEC.listOf().optionalFieldOf("itag").forGetter(geoPlacerSurf -> geoPlacerSurf.biomei),
                    Identifier.CODEC.listOf().optionalFieldOf("biomes").forGetter(geoPlacerSurf -> geoPlacerSurf.biomes)
            ).apply(instance, BiomeY::new));

    private BiomeY(Optional<Integer> spacing,Optional<List<Identifier>> biomet,Optional<List<Identifier>> biomei, Optional<List<Identifier>> biomes) {
        this.spacing = spacing;
        this.biomet=biomet;
        this.biomei=biomei;
        this.biomes=biomes;
    }

    // Factory method to create an instance of BiomeY
    public static BiomeY create(Optional<Integer> spacing,Optional<List<Identifier>> biomet,Optional<List<Identifier>> biomei, Optional<List<Identifier>> biomes) {
        return new BiomeY(spacing,biomet,biomei,biomes);
    }

    @Override
    protected boolean shouldPlace(FeaturePlacementContext context, Random random, BlockPos pos) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int y=spacing.orElse(pos.getY());
        mutable.set(pos.getX(), y, pos.getZ());
        RegistryEntry<Biome> biome = context.getWorld().getBiome(mutable);
        if (biomei.isPresent()) {
            for (Identifier t : biomei.get()){
                TagKey<Biome> tag = TagKey.of(RegistryKeys.BIOME, t);
                if (biome.isIn(tag)) {return false;}
            }
        }
        if (biomes.isPresent()) {
            Identifier biomeId = biome.getKey().orElseThrow().getValue();
            if (biomes.get().contains(biomeId)) {return true;}
        }
        if (biomet.isPresent()) {
            for (Identifier t : biomet.get()){
                TagKey<Biome> tag = TagKey.of(RegistryKeys.BIOME, t);
                if (biome.isIn(tag)) {return true;}
            }
        }
        if(biome.getKey().orElseThrow().getValue().equals(new Identifier("fbased:main"))){return true;}
        return false;
    }

    @Override
    public PlacementModifierType<?> getType() {
        return (PlacementModifierType<?>) FbasedPlacers.FBASED_A2;
    }
}