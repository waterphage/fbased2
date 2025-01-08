package com.waterphage.worldgen.blockstates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;

import static java.lang.Math.abs;

public class Layer16 implements ModPredicate {
    public static final Codec<Layer16> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("layer").forGetter(predicate -> predicate.layer)
                    ).apply(instance, Layer16::new)
    );
    private final int layer;

    public Layer16(int layer) {
        this.layer = layer;
    }

    public boolean test(StructureWorldAccess world, BlockPos pos) {
        int ys=(world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG,pos.getX(),pos.getZ())+world.getHeight()-world.getBottomY())/2;
        int y=pos.getY();
        return layer<=abs(ys-y) % 16;
    }

    @Override
    public BlockPredicateType<?> getType() {
        return ModPredicatesType.A2;
    }
}
