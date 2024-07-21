package com.waterphage.block.worldgen.placers;

import com.mojang.serialization.Codec;
import com.waterphage.Fbased;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

public interface FbasedPlacers extends PlacementModifierType {
    PlacementModifierType<GeoPlacerSurf> FBASED_A0 = register( "tex_s", GeoPlacerSurf.MODIFIER_CODEC);
    PlacementModifierType<GeoPlacer> FBASED_A1 = register( "tex_v", GeoPlacer.MODIFIER_CODEC);
    PlacementModifierType<BiomeY> FBASED_A2 = register( "biome_y", BiomeY.MODIFIER_CODEC);
    PlacementModifierType<Collumn> FBASED_A3 = register( "collumn", Collumn.MODIFIER_CODEC);
    PlacementModifierType<Layer> FBASED_A4 = register( "layer", Layer.MODIFIER_CODEC);

    private static <P extends PlacementModifier> PlacementModifierType<P> register(String id, Codec<P> codec) {
        return Registry.register(Registries.PLACEMENT_MODIFIER_TYPE, new Identifier(Fbased.MOD_ID,id), () -> codec);
    }
    public static void registerplacers(){
    }
}
