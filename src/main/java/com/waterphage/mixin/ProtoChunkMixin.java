package com.waterphage.mixin;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
@Mixin(ProtoChunk.class)
public class ProtoChunkMixin implements ProtoChunkExtension {
    private int[] fbasedHeightMap = new int[256];

    @Override
    public void setHeightMap(int[] heightMap) {
        this.fbasedHeightMap = heightMap;
    }

    @Override
    public int[] getHeightMap() {
        return this.fbasedHeightMap;
    }
}
