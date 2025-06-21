package com.waterphage.meta;

import java.util.Map;
import java.util.TreeMap;

public interface ChunkExtension {
    Map<IntPair, TreeMap<Integer, Integer>> getCustomMap();
    void setCustomMap(Map<IntPair, TreeMap<Integer, Integer>> map);
}
