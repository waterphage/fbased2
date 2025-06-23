package com.waterphage.meta;

import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface ChunkExtension {
    Map<IntPair, TreeMap<Integer, Integer>> getCustomMap();
    void setCustomMap(Map<IntPair, TreeMap<Integer, Integer>> map);
    Map<String, Map<BlockPos, List<BlockPos>>> getExtraGrids();
    void setExtraGrids(Map<String, Map<BlockPos, List<BlockPos>>> grids);
    boolean calculatedFB = false;
    void markAsCalculatedFB();
    boolean getFBstatus();
}
