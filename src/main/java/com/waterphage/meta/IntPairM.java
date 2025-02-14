package com.waterphage.meta;

public class IntPairM implements IntPairBase {
    private int first;
    private int second;

    public IntPairM(int first, int second) {
        this.first = first;
        this.second = second;
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
    public IntPairM set(Integer x,Integer y){
        if(x!=null){this.first=x;}
        if(y!=null){this.second=y;}
        return this;
    }
}

