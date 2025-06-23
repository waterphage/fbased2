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
        if (nbt.contains("ExtraGrids", NbtElement.COMPOUND_TYPE)) {
            NbtCompound grids = nbt.getCompound("ExtraGrids");
            Map<String, Map<BlockPos, List<BlockPos>>> extra = new HashMap<>();

            for (String gridName : grids.getKeys()) {
                Map<BlockPos, List<BlockPos>> map2 = new HashMap<>();
                NbtCompound grid = grids.getCompound(gridName);
                for (String key : grid.getKeys()) {
                    String[] parts = key.split(",");
                    BlockPos base = new BlockPos(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2])
                    );
                    List<BlockPos> list = new ArrayList<>();
                    NbtList arr = grid.getList(key, NbtElement.COMPOUND_TYPE);
                    for (NbtElement el : arr) {
                        NbtCompound tag = (NbtCompound) el;
                        list.add(new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")));
                    }
                    map2.put(base, list);
                }
                extra.put(gridName, map2);
            }
            ext.setExtraGrids(extra);
        }
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

        NbtCompound grids = new NbtCompound();
        for (Map.Entry<String, Map<BlockPos, List<BlockPos>>> gridEntry : ext.getExtraGrids().entrySet()) {
            NbtCompound gridTag = new NbtCompound();
            for (Map.Entry<BlockPos, List<BlockPos>> posEntry : gridEntry.getValue().entrySet()) {
                NbtList list = new NbtList();
                for (BlockPos p : posEntry.getValue()) {
                    NbtCompound pt = new NbtCompound();
                    pt.putInt("x", p.getX());
                    pt.putInt("y", p.getY());
                    pt.putInt("z", p.getZ());
                    list.add(pt);
                }
                gridTag.put(posEntry.getKey().getX() + "," + posEntry.getKey().getY() + "," + posEntry.getKey().getZ(), list);
            }
            grids.put(gridEntry.getKey(), gridTag);
        }
        root.put("ExtraGrids", grids);
        ext.markAsCalculatedFB();
    }
}
