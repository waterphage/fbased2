package com.waterphage.meta;

import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface ChunkExtension {
    Map<IntPair, TreeMap<Integer, Integer>> getCustomMap();
    void setCustomMap(Map<IntPair, TreeMap<Integer, Integer>> map);

    List<Double> getNoise();
    void setNoise(List<Double> map);
}
