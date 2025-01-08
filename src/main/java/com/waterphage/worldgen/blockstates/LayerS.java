package com.waterphage.worldgen.blockstates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;

import static java.lang.Math.abs;

public class LayerS implements ModPredicate {
    public static final Codec<LayerS> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("layer").forGetter(predicate -> predicate.layer),
                    Codec.INT.fieldOf("step").forGetter(predicate -> predicate.step)
                    ).apply(instance, LayerS::new)
    );
    private final int layer;
    private final int step;
    public LayerS(int layer,int step) {
        this.layer = layer;
        this.step = step;
    }

    public boolean test(StructureWorldAccess world, BlockPos pos) {
        int ys=(world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG,pos.getX(),pos.getZ())+world.getHeight()-world.getBottomY())/2;
        int y=pos.getY();
        return layer<=abs(ys-y) % step;
    }

    @Override
    public BlockPredicateType<?> getType() {
        return ModPredicatesType.A2;
    }
}
