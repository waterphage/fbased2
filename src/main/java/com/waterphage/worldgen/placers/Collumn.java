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

public class Collumn extends PlacementModifier {

    public static final Codec<Collumn> MODIFIER_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.INT.fieldOf("spacingY").forGetter(geoPlacer -> geoPlacer.spacing2),
                            Codec.STRING.fieldOf("mode").forGetter(geoPlacer -> geoPlacer.map)
                    )
                    .apply(instance, Collumn::new)
    );
    private final int spacing2;
    private final String map;

    private Collumn(int spacing2, String map) {
        this.spacing2 = spacing2;
        this.map = map;
    }


    public static Collumn of(int spacing2, String map) {
        return new Collumn(spacing2, map);
    }
    public boolean check2(int i, int f) {
        return Math.abs((i) % 2*Math.round(f*Math.pow(0.75F,-0.5F)*0.5F)) < 1;
    }
    public boolean check(int i, int f) {
        return Math.abs((i) % f) < 1;
    }

    @Override
    public Stream<BlockPos> getPositions(FeaturePlacementContext context, Random random, BlockPos pos) {
        StructureWorldAccess world = context.getWorld();
        Stream.Builder<BlockPos> builder = Stream.builder();
        int yo=world.getBottomY();
        try {
            yo = Integer.parseInt(map);
        } catch (NumberFormatException nfe) {
            Heightmap.Type rule = Heightmap.Type.valueOf(map);
            yo = Math.max(yo,(world.getTopY(rule, pos.getX(), pos.getZ()) - 1));
        }
        for (int i=yo;i>=world.getBottomY();--i){
            int sp2 = spacing2;
            boolean test=true;
            if(sp2<1){
                test=true;
            } else{
                test = check(yo-i, sp2);
            }
            if (test) {
                builder.add(new BlockPos(pos.getX(),yo-i,pos.getZ()));
            }
        }
        return builder.build();
    }

    @Override
    public PlacementModifierType<?> getType() {
        return (PlacementModifierType<?>) FbasedPlacers.FBASED_A3;
    }
}
