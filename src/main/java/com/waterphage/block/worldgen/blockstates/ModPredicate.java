package com.waterphage.block.worldgen.blockstates;

import net.minecraft.world.gen.blockpredicate.BlockPredicate;

public interface ModPredicate extends BlockPredicate {
    static BlockPredicate layer(int step, int layer, int shift, Float power, String map) {
        return new Layer(step,layer,shift,power,map);
    }
    static BlockPredicate layer16(int layer) {
        return new Layer16(layer);
    }

}
