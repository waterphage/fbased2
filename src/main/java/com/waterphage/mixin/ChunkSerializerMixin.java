package com.waterphage.mixin;

import com.waterphage.meta.ChunkExtension;
import com.waterphage.meta.FBNMesh;
import com.waterphage.meta.FBXZMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
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

    private static final String[] MESH_TYPES = {"mapSF","inSF","outSF","wallF","mapSC","inSC","outSC","wallC"};

    @Inject(method = "deserialize", at = @At("RETURN"))
    private static void onDeserialize(ServerWorld world, PointOfInterestStorage poiStorage, ChunkPos chunkPos, NbtCompound nbt, CallbackInfoReturnable<ProtoChunk> cir) {
        Chunk chunk = cir.getReturnValue();
        if (!(chunk instanceof ChunkExtension ext)) return;

        if (nbt.contains("FBSurfPos", NbtElement.LONG_ARRAY_TYPE) && nbt.contains("FBSurfTyp", NbtElement.INT_ARRAY_TYPE)) {
            long[] positions = nbt.getLongArray("FBSurfPos");
            int[] types = nbt.getIntArray("FBSurfTyp");
            Long2IntMap map = new Long2IntOpenHashMap(positions.length);
            for (int i = 0; i < positions.length; i++) {
                map.put(positions[i], types[i]);
            }
            ext.setCustomMap(map);

            FBXZMap xzMap = new FBXZMap();
            LongArrayList xz = LongArrayList.wrap(nbt.getLongArray("FBSurfXZ"));
            IntArrayList y = IntArrayList.wrap(nbt.getIntArray("FBSurfY"));
            IntArrayList a = IntArrayList.wrap(nbt.getIntArray("FBSurfA"));
            xzMap.setKeyset(xz);
            xzMap.setValues(y);
            xzMap.setHelper(a);
            xzMap.regenInd(xz);
            ext.setXZmap(xzMap);
        }

        // FIX: Safe noise deserialization avoiding out-of-order index insertion exceptions
        List<Double> noise = new ArrayList<>();
        if (nbt.contains("Noise", NbtElement.COMPOUND_TYPE)) {
            NbtCompound custom = nbt.getCompound("Noise");
            Double[] safeArray = new Double[custom.getKeys().size()];
            for (String key : custom.getKeys()) {
                safeArray[Integer.parseInt(key)] = custom.getDouble(key);
            }
            noise.addAll(Arrays.asList(safeArray));
        }
        ext.setNoise(noise);

        // FIX: Use accurate unique string identifiers instead of String.join
        if (nbt.contains("Meshes", NbtElement.COMPOUND_TYPE)){
            NbtCompound meshes = nbt.getCompound("Meshes");
            for (String type : MESH_TYPES){
                if (!meshes.contains(type + "Key", NbtElement.LONG_ARRAY_TYPE)) continue;
                FBNMesh mesh = new FBNMesh();
                LongArrayList key = LongArrayList.wrap(meshes.getLongArray(type + "Key"));
                LongArrayList val = LongArrayList.wrap(meshes.getLongArray(type + "Value"));
                IntArrayList adr = IntArrayList.wrap(meshes.getIntArray(type + "Adr"));
                mesh.setKeyset(key);
                mesh.setValues(val);
                mesh.setHelper(adr);
                mesh.regenInd(key);
                ext.setNMesh(type, mesh);
            }
        }
    }

    @Inject(method = "serialize", at = @At("RETURN"))
    private static void onSerialize(ServerWorld world, Chunk chunk, CallbackInfoReturnable<NbtCompound> cir) {
        if (!(chunk instanceof ChunkExtension ext)) return;

        Long2IntMap map = ext.getCustomMap();
        FBXZMap XZmap = ext.getXZmap();
        if (map == null || map.isEmpty()) return;

        NbtCompound root = cir.getReturnValue();
        root.putLongArray("FBSurfPos", map.keySet().toLongArray());
        root.putIntArray("FBSurfTyp", map.values().toIntArray());
        root.putLongArray("FBSurfXZ", XZmap.keyset().toLongArray());
        root.putIntArray("FBSurfY", XZmap.values().toIntArray());
        root.putIntArray("FBSurfA", XZmap.helper().toIntArray());

        // FIX: Separated logic validation blocks so missing noise does not choke out the meshes
        List<Double> noise = ext.getNoise();
        if (noise != null && !noise.isEmpty()) {
            NbtCompound param = new NbtCompound();
            int i = 0;
            for (Double value : noise) {
                param.putDouble(String.valueOf(i), value);
                i += 1;
            }
            root.put("Noise", param);
        }

        // FIX: Ensure mesh serialization writes to unique compound property signatures
        if (ext.second()) {
            NbtCompound meshes = new NbtCompound();
            for (String type : MESH_TYPES){
                FBNMesh insert = ext.getNMesh(type);
                if (insert == null || insert.keyset().isEmpty()) continue;
                meshes.putLongArray(type + "Key", insert.keyset().toLongArray());
                meshes.putLongArray(type + "Value", insert.values().toLongArray());
                meshes.putIntArray(type + "Adr", insert.helper().toIntArray());
            }
            if (!meshes.isEmpty()) {
                root.put("Meshes", meshes);
            }
        }
    }
}