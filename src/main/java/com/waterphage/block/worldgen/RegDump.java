package com.waterphage.block.worldgen;

import com.waterphage.Fbased;
import com.waterphage.block.worldgen.blockstates.ModPredicatesType;
import com.waterphage.block.worldgen.feature.*;
import com.waterphage.block.worldgen.placers.FbasedPlacers;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class RegDump {
    public static void registerModChunk() {
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "geo"), new GeoFeature(GeoFeature.GeoFeatureConfig.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "test"), new TestF(TestF.TestFConfig.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "test2"), new TestF2(TestF2.TestF2Config.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "mul"), new Multiple(Multiple.MultipleConfig.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "disk"), new Disk(Disk.DiskConfig.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "fossil"), new Fossil(Fossil.FossilConfig.CODEC));
        Registry.register(Registries.FEATURE, new Identifier(Fbased.MOD_ID, "block"), new Ore(Ore.OreConfig.CODEC));
        FbasedPlacers.registerplacers();
        ModRules.registerrules();
        ModPredicatesType.registerpredicates();
    }
}
