package com.waterphage.mixin;

import com.waterphage.meta.ChunkExtension;
import com.waterphage.meta.FBNMesh;
import com.waterphage.meta.FBXZMap;
import com.waterphage.meta.IntPair;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(ProtoChunk.class) // или Chunk.class — зависит от стадии
public class ProtoChunkMixin implements ChunkExtension {
    @Unique
    BlockPos pos;
    @Override
    public void setPos(BlockPos p) {pos=p;}
    @Override
    public BlockPos getPos() {return pos;}
    @Unique
    private boolean second = false;
    @Override
    public boolean second() {return this.second;}
    @Override
    public void count() {this.second=true;}
    @Unique
    private FBNMesh mapSF = new FBNMesh();
    @Unique
    private FBNMesh inSF = new  FBNMesh();
    @Unique
    private FBNMesh outSF = new FBNMesh();
    @Unique
    private FBNMesh wallF = new FBNMesh();
    @Unique
    private FBNMesh mapSC = new FBNMesh();
    @Unique
    private FBNMesh inSC = new  FBNMesh();
    @Unique
    private FBNMesh outSC = new FBNMesh();
    @Unique
    private FBNMesh wallC = new FBNMesh();
    @Override
    public FBNMesh getNMesh(String id) {
        switch (id){
            case "mapSF":return mapSF;
            case "inSF":return inSF;
            case "outSF":return outSF;
            case "wallF":return wallF;
            case "mapSC":return mapSC;
            case "inSC":return inSC;
            case "outSC":return outSC;
            case "wallC":return wallC;
        }
        return new FBNMesh();
    }

    @Override
    public void setNMesh(String id, FBNMesh mesh) {
        switch (id){
            case "mapSF":this.mapSF=mesh;return;
            case "inSF": this.inSF=mesh;return;
            case "outSF":this.outSF=mesh;return;
            case "wallF":this.wallF=mesh;return;
            case "mapSC":this.mapSC=mesh;return;
            case "inSC": this.inSC=mesh;return;
            case "outSC":this.outSC=mesh;return;
            case "wallC":this.wallC=mesh;return;
        }
    }
    @Unique
    private FBXZMap custXZMap = new FBXZMap();

    @Override
    public FBXZMap getXZmap() {return custXZMap;}
    @Override
    public void setXZmap(FBXZMap XZmap) {this.custXZMap=XZmap;}
    @Unique
    private Long2IntMap customMap = new Long2IntOpenHashMap();
    @Override
    public Long2IntMap getCustomMap() {
        return customMap;
    }
    @Override
    public void setCustomMap(Long2IntMap map) {
        this.customMap = map;
    }
    @Unique
    private List<Double> noise = new ArrayList<>();
    @Override
    public List<Double> getNoise() {return noise;}
    @Override
    public void setNoise(List<Double> map) {
        this.noise = map;
    }
}
