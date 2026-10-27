package com.waterphage.meta;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.noise.NoiseRouter;

public class Storage {
    public static final ColorResolver FOG_COLOR = (biome, x, z) -> biome.getFogColor();
    public static final ColorResolver SKY_COLOR = (biome, x, z) -> biome.getSkyColor();
    public static final ColorResolver GRS_COLOR = (biome, x, z) -> biome.getGrassColorAt(x, z);
    public static final ColorResolver FLG_COLOR = (biome, x, z) -> biome.getFoliageColor();
    public static final ColorResolver WTR_COLOR = (biome, x, z) -> biome.getWaterColor();
    public static final ColorResolver WFG_COLOR = (biome, x, z) -> biome.getWaterFogColor();
    public static final BlockState MIST = Registries.BLOCK.get(new Identifier("fbased", "mist")).getDefaultState();
    public static final Block MIST_B=Registries.BLOCK.get(new Identifier("fbased", "mist"));
    public static final FlowableFluid FLOW = Fluids.FLOWING_WATER;
    private static NoiseRouter params;
    private static <T> Biome get(T w, BlockPos pos){
        BlockRenderView world=(BlockRenderView)w;
        var biome = world.getBiomeFabric(pos);
        if (biome != null) {
            return biome.value();
        }
        if (world instanceof ClientWorld clientWorld) {
            return clientWorld.getBiome(pos).value();
        }
        System.out.println("Biome unavailable: " + world.getClass().getName());
        System.out.println("Interfaces:");
        for (Class<?> iface : world.getClass().getInterfaces()) {
            System.out.println(iface.getName());
        }
        return null;
    }
    private static <P> BlockPos pos(P p){
        if(p instanceof BlockPos pos){return pos;}
        System.out.println("unsupported position feed:"+p.getClass().getName());
        return null;
    }
    public static <T,P> int calculateColor(
            T w,
            P p,
            ColorResolver colorResolver
    ) {
        int radius = MinecraftClient.getInstance()
                .options.getBiomeBlendRadius().getValue();
        BlockPos pos=pos(p);

        if (radius == 0) {
            return colorResolver.getColor(
                    get(w,pos),
                    pos.getX(),
                    pos.getZ()
            );
        }

        int size = radius * 2 + 1;
        int count = size * size;

        int red = 0;
        int green = 0;
        int blue = 0;

        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int x = pos.getX() - radius; x <= pos.getX() + radius; x++) {
            for (int z = pos.getZ() - radius; z <= pos.getZ() + radius; z++) {
                mutable.set(x, pos.getY(), z);
                int color = colorResolver.getColor(get(w,mutable), x, z);
                red += (color >> 16) & 255;
                green += (color >> 8) & 255;
                blue += color & 255;
            }
        }
        return ((red / count) & 255) << 16
                | ((green / count) & 255) << 8
                | ((blue / count) & 255);
    }

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

}
