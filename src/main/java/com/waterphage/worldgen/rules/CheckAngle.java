package com.waterphage.worldgen.rules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.waterphage.worldgen.ModRules;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static net.minecraft.world.Heightmap.Type.OCEAN_FLOOR_WG;

public record CheckAngle(boolean floor, int depth, int r,int min, MaterialRules.MaterialRule surf, MaterialRules.MaterialRule surf_c, MaterialRules.MaterialRule grd,MaterialRules.MaterialRule grd_c) implements MaterialRules.MaterialRule {
        static final CodecHolder<CheckAngle> CODEC = CodecHolder.of(
                RecordCodecBuilder.mapCodec(
                        instance -> instance.group(
                                        Codec.BOOL.fieldOf("floor").forGetter(CheckAngle::floor),
                                        Codec.INT.fieldOf("depth").forGetter(CheckAngle::depth),
                                        Codec.INT.fieldOf("radius").forGetter(CheckAngle::r),
                                        Codec.INT.fieldOf("minimum").forGetter(CheckAngle::min),
                                        MaterialRules.MaterialRule.CODEC.fieldOf("surface").forGetter(CheckAngle::surf),
                                        MaterialRules.MaterialRule.CODEC.fieldOf("cave_surface").forGetter(CheckAngle::surf_c),
                                        MaterialRules.MaterialRule.CODEC.fieldOf("ground").forGetter(CheckAngle::grd),
                                        MaterialRules.MaterialRule.CODEC.fieldOf("cave_ground").forGetter(CheckAngle::grd_c)
                                )
                                .apply(instance, CheckAngle::new)
                )
        );

        public CodecHolder<CheckAngle> CheckAngle(CodecHolder<CheckAngle> codec) {return CODEC;}

        @Override
        public CodecHolder<? extends MaterialRules.MaterialRule> codec() {return CODEC;}

        public MaterialRules.BlockStateRule apply(MaterialRules.MaterialRuleContext cont) {
            Chunk chunk = cont.chunk;
            HeightContext height = cont.heightContext;
            NoiseConfig noise = cont.noiseConfig;
            net.minecraft.util.math.random.Random random = Random.create();

            Stream.Builder<BlockPos> builder_s = Stream.builder();
            Stream.Builder<BlockPos> builder_g = Stream.builder();
            MaterialRules.MaterialRule surface=surf_c;
            MaterialRules.MaterialRule ground=grd_c;
            int xo=cont.pos.getX();int yo=cont.pos.getY();int zo=cont.pos.getZ();
            int c=(int) Math.round(this.r / Math.sqrt(2));
            int[][] offsets = {
                    {0, 0}, {-r, 0}, {r, 0}, {0, -r},
                    {0, r}, {c, c}, {-c, c}, {c, -c}, {-c, -c}
            };
            l:
            for (int[] offset : offsets) {
                int x = xo + offset[0];
                int z = zo + offset[1];
                int topY = cont.chunk.getHeightmap(OCEAN_FLOOR_WG).get(x,z);
                if (yo - topY > min) {
                    surface=surf;
                    ground=grd;
                    break l;
                }
            }
            m:
            if(floor){
                if(!cont.chunk.getBlockState(new BlockPos(xo,yo,zo)).isSolid()){break m;}
                if(cont.chunk.getBlockState(new BlockPos(xo,yo+1,zo)).isSolid()){break m;}
                for (int i=0;i<=depth;i++){
                    if(!cont.chunk.getBlockState(new BlockPos(xo,yo-i-3,zo)).isSolid()){break m;}
                    if(!cont.chunk.getBlockState(new BlockPos(xo-i-1,yo-i-1,zo-i-1)).isSolid()){break m;}
                    if(!cont.chunk.getBlockState(new BlockPos(xo-i-1,yo-i-1,zo+i+1)).isSolid()){break m;}
                    if(!cont.chunk.getBlockState(new BlockPos(xo+i+1,yo-i-1,zo-i-1)).isSolid()){break m;}
                    if(!cont.chunk.getBlockState(new BlockPos(xo+i+1,yo-i-1,zo+i+1)).isSolid()){break m;}
                    if(cont.chunk.getBlockState(new BlockPos(xo-i-1,yo+i+2,zo-i-1)).isSolid()){break m;}
                    if(cont.chunk.getBlockState(new BlockPos(xo-i-1,yo+i+2,zo+i+1)).isSolid()){break m;}
                    if(cont.chunk.getBlockState(new BlockPos(xo+i+1,yo+i+2,zo-i-1)).isSolid()){break m;}
                    if(cont.chunk.getBlockState(new BlockPos(xo+i+1,yo+i+2,zo+i+1)).isSolid()){break m;}
                    if (i==0){builder_s.add(new BlockPos(xo,yo-i,zo));}
                    else{builder_g.add(new BlockPos(xo,yo-i,zo));}
                }
            }else{
                if(!cont.chunk.getBlockState(new BlockPos(xo,yo,zo)).isSolid()){break m;}
                if(cont.chunk.getBlockState(new BlockPos(xo,yo-1,zo)).isSolid()){break m;}
                for (int i=0;i<=depth;i++){
                    if(!cont.chunk.getBlockState(new BlockPos(xo,yo+i+3,zo)).isSolid()){break m;}
                    if(!cont.chunk.getBlockState(new BlockPos(xo-i-1,yo+i+1,zo-i-1)).isSolid()){break m;}
                    if(!cont.chunk.getBlockState(new BlockPos(xo-i-1,yo+i+1,zo+i+1)).isSolid()){break m;}
                    if(!cont.chunk.getBlockState(new BlockPos(xo+i+1,yo+i+1,zo-i-1)).isSolid()){break m;}
                    if(!cont.chunk.getBlockState(new BlockPos(xo+i+1,yo+i+1,zo+i+1)).isSolid()){break m;}
                    if(cont.chunk.getBlockState(new BlockPos(xo-i-1,yo-i-2,zo-i-1)).isSolid()){break m;}
                    if(cont.chunk.getBlockState(new BlockPos(xo-i-1,yo-i-2,zo+i+1)).isSolid()){break m;}
                    if(cont.chunk.getBlockState(new BlockPos(xo+i+1,yo-i-2,zo-i-1)).isSolid()){break m;}
                    if(cont.chunk.getBlockState(new BlockPos(xo+i+1,yo-i-2,zo+i+1)).isSolid()){break m;}
                    if (i==0){builder_s.add(new BlockPos(xo,yo-i,zo));}
                    else{builder_g.add(new BlockPos(xo,yo-i,zo));}
                }
            }
            MaterialRules.MaterialRuleContext context=new MaterialRules.MaterialRuleContext(cont.surfaceBuilder,cont.noiseConfig,cont.chunk,cont.chunkNoiseSampler,po);
            context.pos= (BlockPos.Mutable) new BlockPos(1,2,3);
            return null;
        }
    }
