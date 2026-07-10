package com.waterphage.meta;


import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.Set;

public class FBXZMap {
    private LongArrayList xz=new LongArrayList();
    private IntArrayList y=new IntArrayList();
    private IntArrayList adr=new IntArrayList();
    public LongArrayList keyset(){return xz;}
    public IntArrayList values(){return y;}
    public IntArrayList helper(){return adr;}

    public void setKeyset(LongArrayList xz){this.xz=xz;}
    public void setValues(IntArrayList y){this.y=y;}
    public void setHelper(IntArrayList adr){this.adr=adr;}
    private final Long2IntMap indexMap = new Long2IntOpenHashMap(); // search position map
    private final Int2IntMap chunkMap=new Int2IntOpenHashMap();
    public Int2IntMap chunk(){return this.chunkMap;}
    public void regenInd(LongArrayList xz){
        int i=0;
        for (long l:xz){this.indexMap.put(l,i);i+=1;}
    }
    public FBXZMap() {
        this.adr.add(0);
        this.indexMap.defaultReturnValue(-1);
    }
    public static long xzL(long Lpos){
        return Lpos & 0xFFFFFFFFFFFFF000L;
    }
    public static long xzL(int x,int z){
        return ((long)x & 0x3FFFFFFL) << 38 | ((long)z & 0x3FFFFFFL) << 12;
    }
    public static int yL(long Lpos){
        return (int)(Lpos << 52 >> 52);
    }
    public static int xL(long Lpos) {return (int)(Lpos >> 38);}
    public static int zL(long Lpos) {return (int)(Lpos << 26 >> 38);}
    public static long L(int x, int y, int z) {
        long l = 0L;
        l |= ((long)x & 0x3FFFFFFL) << 38; // 26 bits for X
        l |= ((long)z & 0x3FFFFFFL) << 12; // 26 bits for Z
        l |= ((long)y & 0xFFFL);           // 12 bits for Y
        return l;
    }
    public static long L(long xz, int y) {
        return xz | ((long) y & 0xFFFL);
    }

    public void add(long xz, Set<Integer> y){
        indexMap.put(xz, this.xz.size()); // add search position
        this.xz.add(xz);
        this.y.addAll(y);
        this.adr.add(this.y.size()); // store the new total size as the start index for the NEXT key
    }
    public void addAll(FBXZMap other) {
        int keyOffset = this.xz.size();
        int neigOffset = this.y.size();

        for (int i = 0; i < other.xz.size(); i++) {
            long k = other.xz.getLong(i);
            this.xz.add(k);
            this.indexMap.put(k, keyOffset + i);
        }
        this.y.addAll(other.y);
        for (int i = 1; i < other.adr.size(); i++) {
            this.adr.add(other.adr.getInt(i) + neigOffset);
        }
    }
    public int search(long xz, int y0,boolean m) {
        int idk = indexMap.get(xz);
        if (idk == -1) return -9999; // Or a specific "not found" constan
        int start = adr.getInt(idk);
        int end = (idk + 1 < adr.size()) ? adr.getInt(idk + 1) : y.size();
        int bestY = m?Integer.MAX_VALUE:Integer.MIN_VALUE;
        boolean found = false;
        for (int n = start; n < end; n++) {
            int currentY = this.y.getInt(n);
            // Condition: find the LOWEST y that is still ABOVE y0
            if (m?currentY > y0 && currentY < bestY:currentY < y0 && currentY > bestY) {
                bestY = currentY;
                found = true;
            }
        }
        return found ? bestY : -9999;
    }
    public int search(int x, int z, int y0,boolean m) {
        long xz=xzL(x,z);
        return search(xz,y0,m);
    }
    public int search(long l,boolean m) {
        long xz=xzL(l);
        int y0=yL(l);
        return search(xz,y0,m);
    }

    public Long2IntMap indexMap() {
        return this.indexMap;
    }
}
