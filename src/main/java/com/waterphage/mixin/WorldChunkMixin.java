package com.waterphage.mixin;

import com.waterphage.meta.ChunkExtension;
import com.waterphage.meta.IntPair;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.ProtoChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(WorldChunk.class)
public class WorldChunkMixin implements ChunkExtension{
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

    @Inject(
            method = "<init>(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/ProtoChunk;Lnet/minecraft/world/chunk/WorldChunk$EntityLoader;)V",
            at = @At("TAIL")
    )
    private void copyCustomMap(ServerWorld world, ProtoChunk protoChunk, @Nullable WorldChunk.EntityLoader loader, CallbackInfo ci) {
        if ((Object) this instanceof ChunkExtension self && protoChunk instanceof ChunkExtension proto) {
            self.setCustomMap(proto.getCustomMap());
            self.setNoise(proto.getNoise());
        }
    }
    @Unique
    private List<Double> noise = new ArrayList<>();

    @Override
    public List<Double> getNoise() {
        return noise;
    }

    @Override
    public void setNoise(List<Double> map) {
        this.noise = map;
    }
}
