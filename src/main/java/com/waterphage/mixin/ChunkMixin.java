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
    @Unique
    private Map<String, Map<BlockPos, List<BlockPos>>> extraGrids = new HashMap<>();

    @Override
    public Map<String, Map<BlockPos, List<BlockPos>>> getExtraGrids() {
        return extraGrids;
    }

    @Override
    public void setExtraGrids(Map<String, Map<BlockPos, List<BlockPos>>> grids) {
        this.extraGrids = grids;
    }
    @Unique
    public boolean calculatedFB=false;
    @Override
    public void markAsCalculatedFB(){
        calculatedFB=true;
    }
    @Override
    public boolean getFBstatus(){
        return calculatedFB;
    }
}

