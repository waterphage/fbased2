package com.waterphage.mixin;

import com.waterphage.meta.ChunkExtension;
import com.waterphage.meta.IntPair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;

@Mixin(Chunk.class)
public class ChunkMixin implements ChunkExtension {

    @Unique
    private Map<IntPair, TreeMap<Integer, Integer>> customMap = new HashMap<>();

    @Override
    public Map<IntPair, TreeMap<Integer, Integer>> getCustomMap() {
        return customMap;
    }

    @Override
    public void setCustomMap(Map<IntPair, TreeMap<Integer, Integer>> map) {
        this.customMap = map;
    }
    @Unique
    private Map<String,Double> noise = new HashMap<>();

    @Override
    public Map<String,Double> getNoise() {
        return noise;
    }

    @Override
    public void setNoise(Map<String,Double> map) {
        this.noise = map;
    }
}

