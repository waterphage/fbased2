package com.waterphage.mixin;

import com.waterphage.meta.ChunkExtension;
import com.waterphage.meta.IntPair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
}

