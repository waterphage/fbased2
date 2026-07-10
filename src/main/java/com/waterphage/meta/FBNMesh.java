package com.waterphage.meta;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;

public class FBNMesh {
    private LongArrayList key=new LongArrayList();
    private LongArrayList neig=new LongArrayList();
    private IntArrayList adr=new IntArrayList();
    public LongArrayList keyset(){return key;}
    public LongArrayList values(){return neig;}
    public IntArrayList helper(){return adr;}
    public void setKeyset(LongArrayList xz){this.key=xz;}
    public void setValues(LongArrayList y){this.neig=y;}
    public void setHelper(IntArrayList adr){this.adr=adr;}
    public void regenInd(LongArrayList xz){
        int i=0;
        for (long l:xz){this.indexMap.put(l,i);i+=1;}
    }
    private Long2IntMap indexMap = new Long2IntOpenHashMap(); // search position map
    private Int2IntMap chunkMap=new Int2IntOpenHashMap();
    public Int2IntMap chunk(){return this.chunkMap;}
    public FBNMesh() {
        adr.add(0);
        indexMap.defaultReturnValue(-1);
    }
    public void clear() {
        this.key=new LongArrayList();
        this.neig=new LongArrayList();
        this.adr=new IntArrayList();
        this.indexMap = new Long2IntOpenHashMap(); // search position map
        this.chunkMap=new Int2IntOpenHashMap();
        adr.add(0);
        indexMap.defaultReturnValue(-1);
    }

    public void add(long key, LongArrayList newNeig) {
        indexMap.put(key, this.key.size()); // add search position
        this.key.add(key);
        this.neig.addAll(newNeig);
        this.adr.add(this.neig.size()); // store the new total size as the start index for the NEXT key
    }
    public LongArrayList get(long goal){
        LongArrayList out=new LongArrayList();
        int idk = indexMap.get(goal);
        if (idk==-1){return out;}
        for (int n=adr.getInt(idk);n<adr.getInt(idk+1);++n){
            out.add(this.neig.getLong(n));
        }
        return out;
    }

    public void addAll(FBNMesh other) {
        int keyOffset = this.key.size();
        int neigOffset = this.neig.size();

        for (int i = 0; i < other.key.size(); i++) {
            long k = other.key.getLong(i);
            this.key.add(k);
            this.indexMap.put(k, keyOffset + i);
        }
        this.neig.addAll(other.neig);
        for (int i = 1; i < other.adr.size(); i++) {
            this.adr.add(other.adr.getInt(i) + neigOffset);
        }
    }
}