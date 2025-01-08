package com.waterphage.worldgen.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.waterphage.Fbased;
import com.waterphage.worldgen.feature.GeoFeature;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import org.w3c.dom.DOMImplementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConstantDensityFunction implements DensityFunction {
    private static final MapCodec<ConstantDensityFunction> CONSTANT_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.fieldOf("size").forGetter(ConstantDensityFunction::size),
                    Codec.INT.fieldOf("sites").forGetter(ConstantDensityFunction::pop),
                    Codec.DOUBLE.fieldOf("seed").forGetter(ConstantDensityFunction::seed),
                    Codec.INT.fieldOf("types").forGetter(ConstantDensityFunction::types)
            ).apply(instance, ConstantDensityFunction::new)
    );

    public static final CodecHolder<ConstantDensityFunction> CODEC_HOLDER = DensityFunctionTypes.holderOf(CONSTANT_CODEC);
    private final int size;
    private final int pop;
    private final double seed;
    private final int types;

    public int pop() {return pop;}
    public int size() {return size;}
    public double seed() {return seed;}
    public int types() {return types;}
    public ConstantDensityFunction(int size,int pop,double seed,int types) {
        this.size = size;
        this.pop = pop;
        this.seed = seed;
        this.types=types;
    }

    private static class Backup_Fb_1{
        public int xr=-9999;
        public int zr=-9999;
        public List<BlockPos> sites=new ArrayList<>();
    }
    private final Backup_Fb_1 backup = new Backup_Fb_1();
    @Override
    public double sample(NoisePos pos) {
        int x = pos.blockX();
        int z = pos.blockZ();

        // Check if the cached region matches the current region
        synchronized (backup) {
            if ((backup.xr != x / size) || (backup.zr != z / size)) {
                // Update the cached region
                backup.xr = x / size;
                backup.zr = z / size;

                // Generate a new list of sites for the current and neighboring regions
                List<BlockPos> newSites = new ArrayList<>();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        newSites.addAll(generateSites(backup.xr + dx, backup.zr + dz));
                    }
                }

                // Replace the cached sites
                backup.sites = newSites;
            }
        }
        // Find the closest site
        double closestDistanceSquared = Double.MAX_VALUE;
        int id2 = -1;

        for (BlockPos site : backup.sites) {
            long hash = hashCoordinates(x, z, (long) seed);
            Random random = new Random(hash);
            double dx = site.getX() - x+random.nextInt(size)-size/2;
            double dz = site.getZ() - z+random.nextInt(size)-size/2;
            if (dx * dx + dz * dz >= closestDistanceSquared) continue;

            double distanceSquared = dx * dx + dz * dz;
            if (distanceSquared < closestDistanceSquared) {
                closestDistanceSquared = distanceSquared;
                id2 = site.getY(); // Type
            }
        }

        // Return the type (id2) of the closest site
        return id2;
    }

    private List<BlockPos> generateSites(int regionX, int regionZ) {
        long hash = hashCoordinates(regionX, regionZ, (long) seed);
        Random random = new Random(hash);
        List<BlockPos> sites = new ArrayList<>();
        for (int i = 0; i < pop; i++) {
            int siteX = regionX * size + random.nextInt(size);
            int siteZ = regionZ * size + random.nextInt(size);
            sites.add(new BlockPos(siteX, random.nextInt(types), siteZ));
        }
        return sites;
    }

    private long hashCoordinates(int x, int z, long seed) {
        long h = seed;
        h ^= x * 0x9E3779B185EBCA87L; // Multiply with large primes
        h ^= z * 0xC2B2AE3D27D4EB4FL;
        h ^= (h >>> 33); // Mix bits
        return h;
    }

    @Override
    public double minValue() {return 0;}

    @Override
    public double maxValue() {return types;}
    @Override
    public void fill(double[] densities, EachApplier applier) {
        applier.fill(densities, this);
    }

    @Override
    public DensityFunction apply(DensityFunctionVisitor visitor) {
        return visitor.apply(this);
    }

    @Override
    public CodecHolder<? extends DensityFunction> getCodecHolder() {
        return CODEC_HOLDER;
    }
    public static void register1(){
        Registry.register(Registries.DENSITY_FUNCTION_TYPE, new Identifier(Fbased.MOD_ID, "domains"), CODEC_HOLDER.codec());
    }
}