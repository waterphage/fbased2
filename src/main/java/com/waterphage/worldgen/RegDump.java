package com.waterphage.worldgen;

import com.google.common.cache.Cache;
import com.waterphage.Fbased;
import com.waterphage.worldgen.blockstates.ModPredicatesType;
import com.waterphage.worldgen.feature.*;
import com.waterphage.worldgen.function.ConstantDensityFunction;
import com.waterphage.worldgen.placers.FbasedPlacers;
import it.unimi.dsi.fastutil.objects.Object2ShortArrayMap;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.StructureType;

public class RegDump {
    public static void registerModChunk() {
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "geo"), new GeoFeature(GeoFeature.GeoFeatureConfig.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "test"), new TestF(TestF.TestFConfig.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "test2"), new TestF2(TestF2.TestF2Config.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "mul"), new Order(Multiple.MultipleConfig.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "add"), new Multiple(Multiple.MultipleConfig.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "biome"), new Biome(Biome.BiomeConfig.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "disk"), new Disk(Disk.DiskConfig.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "fossil"), new Fossil(Fossil.FossilConfig.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "block"), new Ore(Ore.OreConfig.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "block_s"), new OreS(OreS.OreSConfig.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "surface"), new Surface(Surface.SurfaceConfig.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "raw_st"), new Stone(Stone.StoneConfig.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "variants"), new Variants(Variants.VariantsConfig.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "fossil_s"), new FossilS(FossilS.FossilSConfig.CODEC));
        FbasedPlacers.registerplacers();
        ModRules.registerrules();
        ModPredicatesType.registerpredicates();
        ConstantDensityFunction.register1();
    }
}
