package com.waterphage.worldgen.blockstates;

import com.mojang.serialization.Codec;
import com.waterphage.Fbased;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;

public final class GeoPType {
    public static final StructureProcessorType<GeoProcessor> GEOP =
            StructureProcessorType.register("fbased:geo", GeoProcessor.CODEC);
    public static void registerprocessors(){}
}
