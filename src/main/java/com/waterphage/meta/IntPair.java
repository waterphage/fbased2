package com.waterphage.meta;

import net.minecraft.nbt.NbtCompound;

public record IntPair(int first, int second) implements IntPairBase, Comparable<IntPair> {

    public NbtCompound toNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putInt("first", first);
        tag.putInt("second", second);
        return tag;
    }

    public static IntPair fromNbt(NbtCompound tag) {
        return new IntPair(tag.getInt("first"), tag.getInt("second"));
    }

    @Override
    public int compareTo(IntPair other) {
        int cmp = Integer.compare(this.first, other.first);
        return cmp != 0 ? cmp : Integer.compare(this.second, other.second);
    }

    // Для стабильности работы в Map / TreeMap
    @Override
    public boolean equals(Object o) {
        return o instanceof IntPair ip && ip.first == this.first && ip.second == this.second;
    }

    @Override
    public int hashCode() {
        return 31 * first + second;
    }
}
