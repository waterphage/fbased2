package com.waterphage.meta;

import net.minecraft.nbt.NbtCompound;

public interface IntPairBase {
    int first();
    int second();

    default IntPairBase add(int x, int y) {
        throw new UnsupportedOperationException("Use IntPairM");
    }

    default IntPairBase set(Integer x, Integer y) {
        throw new UnsupportedOperationException("Use IntPairM");
    }

    /**
     * Универсальная сериализация в NBT
     */
    default NbtCompound toNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putInt("x", first());
        tag.putInt("z", second());
        return tag;
    }

    /**
     * Универсальная десериализация из NBT
     * @param tag источник
     * @param mutable если true — вернёт IntPairM, иначе IntPair
     */
    static IntPairBase fromNbt(NbtCompound tag, boolean mutable) {
        int x = tag.getInt("x");
        int z = tag.getInt("z");
        return mutable ? new IntPairM(x, z) : new IntPair(x, z);
    }
}
