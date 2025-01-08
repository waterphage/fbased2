package com.waterphage.worldgen.placers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.AbstractConditionalPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

import java.util.stream.Stream;

public class Shadow extends PlacementModifier {

    public static final Codec<Shadow> MODIFIER_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.INT.fieldOf("radius").forGetter(Shadow -> Shadow.r),
                            Codec.INT.fieldOf("min").forGetter(Shadow -> Shadow.min),
                            Codec.STRING.fieldOf("mode").forGetter(Shadow -> Shadow.map)
                    )
                    .apply(instance, Shadow::new)
    );
    private final int r;
    private final int min;
    private final String map;

    private Shadow(int r, int min, String map) {
        this.r = r;
        this.min = min;
        this.map = map;
    }
    public static Shadow of(int r,int min, String map) {
        return new Shadow(r,min, map);
    }
    @Override
    public Stream<BlockPos> getPositions(FeaturePlacementContext context, Random random, BlockPos pos) {
        StructureWorldAccess world = context.getWorld();
        Stream.Builder<BlockPos> builder = Stream.builder();
        Heightmap.Type rule = Heightmap.Type.valueOf(map);
        int xo=pos.getX();int yo=pos.getY();int zo=pos.getZ();
        int c=(int) Math.round(this.r / Math.sqrt(2));
        int[][] offsets = {
                {0, 0}, {-r, 0}, {r, 0}, {0, -r},
                {0, r}, {c, c}, {-c, c}, {c, -c}, {-c, -c}
        };

        for (int[] offset : offsets) {
            int x = xo + offset[0];
            int z = zo + offset[1];
            int topY = world.getTopY(rule, x, z);

            if (yo - topY > min) {
                builder.add(new BlockPos(xo, yo, zo));
                return builder.build();
            }
        }
        return builder.build();
    }

    @Override
    public PlacementModifierType<?> getType() {
        return (PlacementModifierType<?>) FbasedPlacers.FBASED_A9;
    }
}
