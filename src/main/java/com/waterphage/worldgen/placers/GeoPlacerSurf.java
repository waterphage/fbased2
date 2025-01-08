package com.waterphage.worldgen.placers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

import java.util.stream.Stream;

public class GeoPlacerSurf extends PlacementModifier {

    public static final Codec<GeoPlacerSurf> MODIFIER_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.INT.fieldOf("spacing").forGetter(GeoPlacerSurf::getSpacing),
                            Codec.STRING.fieldOf("mode").forGetter(GeoPlacerSurf::getMap),
                            Codec.STRING.fieldOf("mesh").forGetter(GeoPlacerSurf::getMesh)
                    )
                    .apply(instance, GeoPlacerSurf::new)
    );
    private final int spacing;
    private final String map;
    private final String mesh;

    private GeoPlacerSurf(int spacing, String map, String mesh) {
        this.spacing = spacing;
        this.map = map;
        this.mesh = mesh;
    }

    public static GeoPlacerSurf of(int spacing, String map, String mesh) {
        return new GeoPlacerSurf(spacing, map, mesh);
    }

    public int getSpacing() {
        return spacing;
    }

    public String getMap() {
        return map;
    }

    public String getMesh() {
        return mesh;
    }

    public boolean check2(int i, int f) {
        return Math.abs((i) % (2 * Math.round(f * Math.pow(0.75F, -0.5F) * 0.5F))) < 1;
    }

    public boolean check(int i, int f) {
        return Math.abs(i % f) < 1;
    }

    @Override
    public Stream<BlockPos> getPositions(FeaturePlacementContext context, Random random, BlockPos pos) {
        StructureWorldAccess world = context.getWorld();
        Stream.Builder<BlockPos> builder = Stream.builder();
        int xo = pos.getX();
        int zo = pos.getZ();

        for (int j = 0; j < 16; ++j) {
            int xn = xo - j + 7;
            for (int k = 0; k < 16; ++k) {
                int zn = zo - k + 7;
                int yo = determineY(world, xn, zn);

                if (shouldPlace(xn, zn)) {
                    builder.add(new BlockPos(xn, yo, zn));
                }
            }
        }

        return builder.build();
    }

    private int determineY(StructureWorldAccess world, int x, int z) {
        try {
            return Integer.parseInt(map);
        } catch (NumberFormatException e) {
            Heightmap.Type rule = Heightmap.Type.valueOf(map);
            return world.getTopY(rule, x, z) - 1;
        }
    }

    private boolean shouldPlace(int x, int z) {
        switch (mesh) {
            case "full":
                return true;
            case "square":
                return check(x, spacing) && check(z, spacing);
            case "romb":
                return check(x + z, spacing) && check(x - z, spacing);
            case "square_c":
                return (check(x, spacing) && check(z, spacing)) || (check(x + spacing, spacing) && check(z + spacing, spacing));
            case "hex":
                return (check2(x, spacing) && check(z, Math.round(spacing / 2.0f))) || (check2(x + spacing, spacing) && check(z + Math.round(spacing / 2.0f), Math.round(spacing / 2.0f)));
            case "hex_a":
                return (check2(z, spacing) && check(x, Math.round(spacing / 2.0f))) || (check2(z + spacing, spacing) && check(x + Math.round(spacing / 2.0f), Math.round(spacing / 2.0f)));
            default:
                return false;
        }
    }

    @Override
    public PlacementModifierType<?> getType() {
        return (PlacementModifierType<?>) FbasedPlacers.FBASED_A0;
    }
}