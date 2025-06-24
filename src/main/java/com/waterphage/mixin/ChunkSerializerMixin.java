package com.waterphage.mixin;

import com.waterphage.meta.ChunkExtension;
import com.waterphage.meta.IntPair;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin {

    @Inject(method = "deserialize", at = @At("RETURN"))
    private static void onDeserialize(ServerWorld world, PointOfInterestStorage poiStorage, ChunkPos chunkPos, NbtCompound nbt, CallbackInfoReturnable<ProtoChunk> cir) {
        Chunk chunk = cir.getReturnValue();
        if (!(chunk instanceof ChunkExtension ext)) return;

        Map<IntPair, TreeMap<Integer, Integer>> map = new HashMap<>();
        if (nbt.contains("SurfaceCustom", NbtElement.COMPOUND_TYPE)) {
            NbtCompound custom = nbt.getCompound("SurfaceCustom");
            for (String key : custom.getKeys()) {
                String[] parts = key.split(",");
                int x = Integer.parseInt(parts[0]);
                int z = Integer.parseInt(parts[1]);
                TreeMap<Integer, Integer> inner = new TreeMap<>();
                NbtCompound values = custom.getCompound(key);
                for (String y : values.getKeys()) {
                    inner.put(Integer.parseInt(y), values.getInt(y));
                }
                map.put(new IntPair(x, z), inner);
            }
        }
        ext.setCustomMap(map);
    }

    @Inject(method = "serialize", at = @At("RETURN"))
    private static void onSerialize(ServerWorld world, Chunk chunk, CallbackInfoReturnable<NbtCompound> cir) {
        if (!(chunk instanceof ChunkExtension ext)) return;

        Map<IntPair, TreeMap<Integer, Integer>> map = ext.getCustomMap();
        if (map == null || map.isEmpty()) return;

        NbtCompound root = cir.getReturnValue();
        NbtCompound surface = new NbtCompound();
        for (Map.Entry<IntPair, TreeMap<Integer, Integer>> entry : map.entrySet()) {
            NbtCompound inner = new NbtCompound();
            for (Map.Entry<Integer, Integer> value : entry.getValue().entrySet()) {
                inner.putInt(String.valueOf(value.getKey()), value.getValue());
            }
            surface.put(entry.getKey().first() + "," + entry.getKey().second(), inner);
        }
        root.put("SurfaceCustom", surface);
    }
}
