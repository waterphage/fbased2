package com.waterphage.meta;

public interface IntPairBase {
    int first();
    int second();

    default IntPairBase add(int x, int y) {
        throw new UnsupportedOperationException("Plese use IntPairM here");
    }

    default IntPairBase set(Integer x, Integer y) {
        throw new UnsupportedOperationException("Plese use IntPairM here");
    }
}

