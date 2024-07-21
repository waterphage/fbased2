package com.waterphage.block.worldgen.placers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

import java.util.List;
import java.util.stream.Stream;

import static java.lang.Math.abs;

public class Layer extends PlacementModifier {

    public static final Codec<Layer> MODIFIER_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.INT.fieldOf("step").forGetter(Layer -> Layer.step),
                            Codec.INT.listOf().fieldOf("layer").forGetter(Layer -> Layer.layer),
                            Codec.INT.fieldOf("shift").forGetter(Layer -> Layer.shift),
                            Codec.STRING.fieldOf("mode").forGetter(Layer -> Layer.map)
                    )
                    .apply(instance, Layer::new)
    );
    private final int step;
    private final List<Integer> layer;
    private final int shift;
    private final String map;

    private Layer(int step,List<Integer> layer,int shift, String map) {
        this.step = step;
        this.layer = layer;
        this.shift=shift;
        this.map = map;
    }


    public static Layer of(int step,List<Integer> layer,int shift, String map) {
        return new Layer(step,layer,shift,map);
    }
    @Override
    public Stream<BlockPos> getPositions(FeaturePlacementContext context, Random random, BlockPos pos) {
        StructureWorldAccess world = context.getWorld();
        Stream.Builder<BlockPos> builder = Stream.builder();
        int yb=world.getBottomY();
        int ym=world.getHeight()-world.getBottomY();
        int ys=(world.getTopY(Heightmap.Type.valueOf(map),pos.getX(),pos.getZ())+ym)/2;
        int y=pos.getY();
        for (int layer_el:layer) {
            boolean test=layer_el==abs(y-ys+shift) % step;
            if (test) {
                builder.add(new BlockPos(pos.getX(),pos.getY(),pos.getZ()));
            }
        }
        return builder.build();
    }

    @Override
    public PlacementModifierType<?> getType() {
        return (PlacementModifierType<?>) FbasedPlacers.FBASED_A4;
    }
}
