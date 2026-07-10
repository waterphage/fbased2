package com.waterphage.meta;

import net.minecraft.util.Identifier;

import java.util.List;

public interface FBGeoMap {
    List<Identifier> get();
    void set(List<Identifier> map);
}
