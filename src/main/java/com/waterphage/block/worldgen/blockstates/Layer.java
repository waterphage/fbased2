package com.waterphage.block.worldgen.blockstates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;

import static java.lang.Math.abs;
import static java.lang.Math.round;

public class Layer implements ModPredicate {
    public static final Codec<Layer> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("s").forGetter(predicate -> predicate.step),
                    Codec.INT.fieldOf("l").forGetter(predicate -> predicate.layer),
                    Codec.INT.fieldOf("o").forGetter(predicate -> predicate.shift),
                    Codec.FLOAT.fieldOf("p").forGetter(predicate -> predicate.power),
                    Codec.STRING.fieldOf("mode").forGetter(predicate -> predicate.map)
                    ).apply(instance, Layer::new)
    );

    private final int step;
    private final int layer;
    private final int shift;
    private final Float power;
    private final String map;

    public Layer(int step, int layer, int shift, Float power, String map) {
        this.step = step;
        this.layer = layer;
        this.shift = shift;
        this.power = power;
        this.map = map;
    }

    public boolean test(StructureWorldAccess world, BlockPos pos) {
        int ys=round(power*world.getTopY(Heightmap.Type.valueOf(map),pos.getX(),pos.getZ())+(1-power)*(world.getHeight()-world.getBottomY()));
        int y=pos.getY();
        return layer<=abs(ys-y+shift) % step;
    }

    @Override
    public BlockPredicateType<?> getType() {
        return ModPredicatesType.A1;
    }
}
