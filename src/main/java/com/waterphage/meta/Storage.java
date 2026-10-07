package com.waterphage.meta;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.noise.NoiseRouter;

public class Storage {
    private static NoiseRouter params;

    public static void write(NoiseRouter params) {
        Storage.params = params;
    }

    public static NoiseRouter params() {
        return params;
    }
    // Enum to map different noise types to their respective functions in the NoiseRouter
    public enum NoiseType {
        TEMP {@Override public DensityFunction getNoise(NoiseRouter params) {
            return params.temperature();
        }},
        CONT {@Override public DensityFunction getNoise(NoiseRouter params) {
            return params.continents();
        }},
        WERD {@Override public DensityFunction getNoise(NoiseRouter params) {
            return params.ridges();
        }},
        HUM {@Override public DensityFunction getNoise(NoiseRouter params) {
            return params.vegetation();
        }},
        EROS {@Override public DensityFunction getNoise(NoiseRouter params) {
            return params.erosion();
        }},
        DEPT {@Override public DensityFunction getNoise(NoiseRouter params) {
            return params.depth();
        }},
        INIT {@Override public DensityFunction getNoise(NoiseRouter params) {return params.initialDensityWithoutJaggedness();}},
        LAVA {@Override public DensityFunction getNoise(NoiseRouter params) {return params.lavaNoise();}},
        SPRD {@Override public DensityFunction getNoise(NoiseRouter params) {
            return params.fluidLevelSpreadNoise();
        }},
        FLOD {@Override public DensityFunction getNoise(NoiseRouter params) {return params.fluidLevelFloodednessNoise();}},
        BARR {@Override public DensityFunction getNoise(NoiseRouter params) {return params.barrierNoise();}},
        VRID {@Override public DensityFunction getNoise(NoiseRouter params) {return params.veinRidged();}},
        VGAP {@Override public DensityFunction getNoise(NoiseRouter params) {return params.veinGap();}},
        VTOG {@Override public DensityFunction getNoise(NoiseRouter params) {return params.veinToggle();}},
        FIN {@Override public DensityFunction getNoise(NoiseRouter params) {return params.finalDensity();}};

        // Abstract method to be implemented by all noise types
        public abstract DensityFunction getNoise(NoiseRouter params);
        public static final Codec<NoiseType> CODEC = Codec.STRING.xmap(NoiseType::fromString, Enum::name);
        // Map a string to its corresponding NoiseType enum value
        private static NoiseType fromString(String name) {
            return switch (name.toLowerCase()) {
                case "temperature" -> NoiseType.TEMP;
                case "humidity" -> NoiseType.HUM;
                case "continentalness" -> NoiseType.CONT;
                case "erosion" -> NoiseType.EROS;
                case "depth" -> NoiseType.DEPT;
                case "ridges" -> NoiseType.WERD;
                case "barrier" -> NoiseType.BARR;
                case "fluid_level_floodedness" -> NoiseType.FLOD;
                case "fluid_level_spread" -> NoiseType.SPRD;
                case "lava" -> NoiseType.LAVA;
                case "initial_density_without_jaggedness" -> NoiseType.INIT;
                case "final_density" -> NoiseType.FIN;
                case "vein_ridged" -> NoiseType.VRID;
                case "vein_toggle" -> NoiseType.VTOG;
                case "vein_gap" -> NoiseType.VGAP;
                default -> throw new IllegalArgumentException("Unknown noise type: " + name);
            };
        }
    }
    public static final TagKey<Biome> FB_HOT =
            TagKey.of(RegistryKeys.BIOME, new Identifier("fbased", "hot"));

    public static final TagKey<Biome> FB_COLD =
            TagKey.of(RegistryKeys.BIOME, new Identifier("fbased", "cold"));

    public static final TagKey<Biome> FB_DRY =
            TagKey.of(RegistryKeys.BIOME, new Identifier("fbased", "dry"));

    public static final TagKey<Biome> FB_WET =
            TagKey.of(RegistryKeys.BIOME, new Identifier("fbased", "wet"));
}
