package com.waterphage.mixin;

import com.waterphage.meta.ChunkExtension;
import com.waterphage.meta.IntPair;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Mixin(ProtoChunk.class) // или Chunk.class — зависит от стадии
public class ProtoChunkMixin implements ChunkExtension {
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
