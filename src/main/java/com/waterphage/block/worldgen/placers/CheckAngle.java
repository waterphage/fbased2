package com.waterphage.block.worldgen.placers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.placementmodifier.AbstractConditionalPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CheckAngle extends PlacementModifier {
    private boolean spacing;
    private int depth;
    public static final Codec<CheckAngle> MODIFIER_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.BOOL.fieldOf("floor").forGetter(CheckAngle -> CheckAngle.spacing),
                            Codec.INT.fieldOf("depth").forGetter(CheckAngle -> CheckAngle.depth)
                    )
                    .apply(instance, CheckAngle::new)
    );

    private CheckAngle(boolean spacing,int depth) {
        this.depth=depth;
        this.spacing = spacing;
    }

    // Factory method to create an instance of BiomeY
    public static CheckAngle create(boolean spacing,int depth) {
        return new CheckAngle(spacing,depth);
    }


    @Override
    public Stream<BlockPos> getPositions(FeaturePlacementContext context, Random random, BlockPos pos) {

        StructureWorldAccess world = context.getWorld();
        Stream.Builder<BlockPos> builder = Stream.builder();
        int x=pos.getX();int y=pos.getY();int z=pos.getZ();
        m:
        if(spacing){
            if(!world.getBlockState(new BlockPos(x,y,z)).isSolid()){break m;}
            if(world.getBlockState(new BlockPos(x,y+1,z)).isSolid()){break m;}
            for (int i=1;i<=depth;i++){
                if(!world.getBlockState(new BlockPos(x,y-i-3,z)).isSolid()){break m;}
                if(!world.getBlockState(new BlockPos(x-i-1,y-i-1,z-i-1)).isSolid()){break m;}
                if(!world.getBlockState(new BlockPos(x-i-1,y-i-1,z+i+1)).isSolid()){break m;}
                if(!world.getBlockState(new BlockPos(x+i+1,y-i-1,z-i-1)).isSolid()){break m;}
                if(!world.getBlockState(new BlockPos(x+i+1,y-i-1,z+i+1)).isSolid()){break m;}
                if(world.getBlockState(new BlockPos(x-i-1,y+i+2,z-i-1)).isSolid()){break m;}
                if(world.getBlockState(new BlockPos(x-i-1,y+i+2,z+i+1)).isSolid()){break m;}
                if(world.getBlockState(new BlockPos(x+i+1,y+i+2,z-i-1)).isSolid()){break m;}
                if(world.getBlockState(new BlockPos(x+i+1,y+i+2,z+i+1)).isSolid()){break m;}
                builder.add(new BlockPos(x,y-i,z));
            }
        }else{
            if(!world.getBlockState(new BlockPos(x,y,z)).isSolid()){break m;}
            if(world.getBlockState(new BlockPos(x,y-1,z)).isSolid()){break m;}
            for (int i=1;i<=depth;i++){
                if(!world.getBlockState(new BlockPos(x,y+i+3,z)).isSolid()){break m;}
                if(!world.getBlockState(new BlockPos(x-i-1,y+i+1,z-i-1)).isSolid()){break m;}
                if(!world.getBlockState(new BlockPos(x-i-1,y+i+1,z+i+1)).isSolid()){break m;}
                if(!world.getBlockState(new BlockPos(x+i+1,y+i+1,z-i-1)).isSolid()){break m;}
                if(!world.getBlockState(new BlockPos(x+i+1,y+i+1,z+i+1)).isSolid()){break m;}
                if(world.getBlockState(new BlockPos(x-i-1,y-i-2,z-i-1)).isSolid()){break m;}
                if(world.getBlockState(new BlockPos(x-i-1,y-i-2,z+i+1)).isSolid()){break m;}
                if(world.getBlockState(new BlockPos(x+i+1,y-i-2,z-i-1)).isSolid()){break m;}
                if(world.getBlockState(new BlockPos(x+i+1,y-i-2,z+i+1)).isSolid()){break m;}
                builder.add(new BlockPos(x,y+i,z));
            }
        }
        return builder.build();
    }

    @Override
    public PlacementModifierType<?> getType() {
        return (PlacementModifierType<?>) FbasedPlacers.FBASED_A5;
    }
}