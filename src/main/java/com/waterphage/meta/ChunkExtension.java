package com.waterphage.meta;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;

public interface ChunkExtension {
    BlockPos pos = null;
    void setPos(BlockPos p);
    BlockPos getPos();
    boolean second();
    void count();
    FBNMesh getNMesh(String id);
    void setNMesh(String id,FBNMesh mesh);
    FBXZMap getXZmap();
    void setXZmap(FBXZMap XZmap);
    Long2IntMap getCustomMap();
    void setCustomMap(Long2IntMap map);

    List<Double> getNoise();
    void setNoise(List<Double> map);
}
