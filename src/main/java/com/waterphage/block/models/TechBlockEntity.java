package com.waterphage.block.models;

import com.waterphage.meta.IntPair;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.util.CaveSurface;

import java.util.*;

import java.util.ArrayList;

public class TechBlockEntity extends BlockEntity {

    private final Map<Integer, Integer> pairs = new HashMap<>();

    public TechBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TECH_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        int[] pairArray = tag.getIntArray("Pairs");
        pairs.clear();
        for (int i = 0; i < pairArray.length; i += 2) {
            pairs.put(pairArray[i], pairArray[i + 1]);
        }
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        int[] pairArray = new int[pairs.size() * 2];
        int index = 0;
        for (Map.Entry<Integer, Integer> entry : pairs.entrySet()) {
            pairArray[index++] = entry.getKey();
            pairArray[index++] = entry.getValue();
        }
        tag.putIntArray("Pairs", pairArray);
    }

    public void addPair(int y, int i) {
        pairs.put(y, i); // Add or update the pair
        markDirty();
    }

    public Map<Integer, Integer> getPairs() {
        return pairs;
    }
    public void setPairs(Map<Integer, Integer> loc) {
        pairs.clear();
        pairs.putAll(loc);
        markDirty();
    }
}