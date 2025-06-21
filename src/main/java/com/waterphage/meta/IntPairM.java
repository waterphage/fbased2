package com.waterphage.meta;

import net.minecraft.nbt.NbtCompound;

public class IntPairM implements IntPairBase {

    private int first;
    private int second;

    public IntPairM(int first, int second) {
        this.first = first;
        this.second = second;
    }

    public static IntPairM fromNbt(NbtCompound tag) {
        return new IntPairM(tag.getInt("x"), tag.getInt("z"));
    }

    public NbtCompound toNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putInt("x", first);
        tag.putInt("z", second);
        return tag;
    }

    @Override
    public int first() {
        return first;
    }

    @Override
    public int second() {
        return second;
    }

    @Override
    public IntPairM add(int x, int y) {
        this.first += x;
        this.second += y;
        return this;
    }

    @Override
    public IntPairM set(Integer x, Integer y) {
        if (x != null) this.first = x;
        if (y != null) this.second = y;
        return this;
    }

    public IntPair toImmutable() {
        return new IntPair(first, second);
    }
}
