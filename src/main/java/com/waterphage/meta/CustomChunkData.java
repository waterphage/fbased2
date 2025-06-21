package com.waterphage.meta;

import net.minecraft.nbt.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.datafixer.DataFixTypes;

import java.util.*;

public class CustomChunkData extends PersistentState {

    // Основная структура: карта чанков, где каждому ChunkPos соответствует вложенная карта IntPair -> TreeMap<Y, Value>
    private final Map<ChunkPos, Map<IntPair, TreeMap<Integer, Integer>>> data = new HashMap<>();

    // Статический TYPE для getOrCreate
    public static final PersistentState.Type<CustomChunkData> TYPE =
            new PersistentState.Type<>(
                    CustomChunkData::new, // from Nbt
                    CustomChunkData::new, // blank instance
                    null // Без DataFixTypes
            );


    // Пустой конструктор
    public CustomChunkData() {}

    // Загрузка из NBT
    public CustomChunkData(NbtCompound nbt) {
        NbtList chunkList = nbt.getList("chunks", NbtElement.COMPOUND_TYPE);
        for (NbtElement el : chunkList) {
            NbtCompound chunkTag = (NbtCompound) el;
            int x = chunkTag.getInt("x");
            int z = chunkTag.getInt("z");
            ChunkPos chunkPos = new ChunkPos(x, z);

            Map<IntPair, TreeMap<Integer, Integer>> chunkData = new HashMap<>();

            NbtList entries = chunkTag.getList("entries", NbtElement.COMPOUND_TYPE);
            for (NbtElement entryEl : entries) {
                NbtCompound pairTag = (NbtCompound) entryEl;
                IntPair pair = IntPair.fromNbt(pairTag.getCompound("pos"));

                TreeMap<Integer, Integer> values = new TreeMap<>();
                NbtList valueList = pairTag.getList("values", NbtElement.COMPOUND_TYPE);
                for (NbtElement val : valueList) {
                    NbtCompound v = (NbtCompound) val;
                    values.put(v.getInt("k"), v.getInt("v"));
                }

                chunkData.put(pair, values);
            }

            data.put(chunkPos, chunkData);
        }
    }

    // Получить или создать карту данных чанка
    public Map<IntPair, TreeMap<Integer, Integer>> getOrCreate(ChunkPos pos) {
        return data.computeIfAbsent(pos, __ -> new HashMap<>());
    }

    // Задать данные вручную и отметить dirty
    public void set(ChunkPos pos, Map<IntPair, TreeMap<Integer, Integer>> value) {
        data.put(pos, value);
        markDirty();
    }

    // Сохранение в NBT
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList chunkList = new NbtList();

        for (Map.Entry<ChunkPos, Map<IntPair, TreeMap<Integer, Integer>>> chunkEntry : data.entrySet()) {
            ChunkPos chunkPos = chunkEntry.getKey();
            NbtCompound chunkTag = new NbtCompound();
            chunkTag.putInt("x", chunkPos.x);
            chunkTag.putInt("z", chunkPos.z);

            NbtList pairsList = new NbtList();
            for (Map.Entry<IntPair, TreeMap<Integer, Integer>> pairEntry : chunkEntry.getValue().entrySet()) {
                NbtCompound pairTag = new NbtCompound();
                pairTag.put("pos", pairEntry.getKey().toNbt());

                NbtList values = new NbtList();
                for (Map.Entry<Integer, Integer> value : pairEntry.getValue().entrySet()) {
                    NbtCompound v = new NbtCompound();
                    v.putInt("k", value.getKey());
                    v.putInt("v", value.getValue());
                    values.add(v);
                }

                pairTag.put("values", values);
                pairsList.add(pairTag);
            }

            chunkTag.put("entries", pairsList);
            chunkList.add(chunkTag);
        }

        nbt.put("chunks", chunkList);
        return nbt;
    }

    // Получить экземпляр из мира
    public static CustomChunkData get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE, "custom_chunk_data");
    }
}
