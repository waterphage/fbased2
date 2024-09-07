package com.waterphage.block.worldgen.placers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

import java.util.stream.Stream;

public class CheckAngleS extends PlacementModifier {
    private boolean spacing;
    public static final Codec<CheckAngleS> MODIFIER_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.BOOL.fieldOf("floor").forGetter(CheckAngle -> CheckAngle.spacing)
                    )
                    .apply(instance, CheckAngleS::new)
    );

    private CheckAngleS(boolean spacing) {
        this.spacing = spacing;
    }

    // Factory method to create an instance of BiomeY
    public static CheckAngleS create(boolean spacing) {
        return new CheckAngleS(spacing);
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
            if(!world.getBlockState(new BlockPos(x,y-3,z)).isSolid()){break m;}
            if(!world.getBlockState(new BlockPos(x-1,y-1,z-1)).isSolid()){break m;}
            if(!world.getBlockState(new BlockPos(x-1,y-1,z+1)).isSolid()){break m;}
            if(!world.getBlockState(new BlockPos(x+1,y-1,z-1)).isSolid()){break m;}
            if(!world.getBlockState(new BlockPos(x+1,y-1,z+1)).isSolid()){break m;}
            if(world.getBlockState(new BlockPos(x-1,y+2,z-1)).isSolid()){break m;}
            if(world.getBlockState(new BlockPos(x-1,y+2,z+1)).isSolid()){break m;}
            if(world.getBlockState(new BlockPos(x+1,y+2,z-1)).isSolid()){break m;}
            if(world.getBlockState(new BlockPos(x+1,y+2,z+1)).isSolid()){break m;}
            builder.add(new BlockPos(x,y,z));
        }else{
            if(!world.getBlockState(new BlockPos(x,y,z)).isSolid()){break m;}
            if(world.getBlockState(new BlockPos(x,y-1,z)).isSolid()){break m;}
            if(!world.getBlockState(new BlockPos(x,y+3,z)).isSolid()){break m;}
            if(!world.getBlockState(new BlockPos(x-1,y+1,z-1)).isSolid()){break m;}
            if(!world.getBlockState(new BlockPos(x-1,y+1,z+1)).isSolid()){break m;}
            if(!world.getBlockState(new BlockPos(x+1,y+1,z-1)).isSolid()){break m;}
            if(!world.getBlockState(new BlockPos(x+1,y+1,z+1)).isSolid()){break m;}
            if(world.getBlockState(new BlockPos(x-1,y-2,z-1)).isSolid()){break m;}
            if(world.getBlockState(new BlockPos(x-1,y-2,z+1)).isSolid()){break m;}
            if(world.getBlockState(new BlockPos(x+1,y-2,z-1)).isSolid()){break m;}
            if(world.getBlockState(new BlockPos(x+1,y-2,z+1)).isSolid()){break m;}
            builder.add(new BlockPos(x,y,z));
        }
        return builder.build();
    }

    @Override
    public PlacementModifierType<?> getType() {
        return (PlacementModifierType<?>) FbasedPlacers.FBASED_A6;
    }
}