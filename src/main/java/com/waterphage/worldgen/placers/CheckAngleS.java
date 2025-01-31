package com.waterphage.worldgen.placers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
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
        this.hash = computeHash();
    }
    private static final Map<Integer, CheckAngleS> CACHE = new ConcurrentHashMap<>();
    private final int hash;
    private int computeHash() {return Objects.hash(spacing);}
    public static CheckAngleS create(boolean spacing) {
        return CACHE.computeIfAbsent(new CheckAngleS(spacing).hash, hash -> new CheckAngleS(spacing));
    }

    private boolean state(BlockPos pos,StructureWorldAccess world){
        return world.getBlockState(pos).isSolid();
    }

    @Override
    public Stream<BlockPos> getPositions(FeaturePlacementContext context, Random random, BlockPos pos) {

        StructureWorldAccess world = context.getWorld();
        Stream.Builder<BlockPos> builder = Stream.builder();
        int x=pos.getX();int y=pos.getY();int z=pos.getZ();
        int m = spacing ? 1:-1;
        if(!world.getBlockState(new BlockPos(x,y,z)).isSolid()){return builder.build();}
        if(world.getBlockState(new BlockPos(x,y+m*1,z)).isSolid()){return builder.build();}
        if(!world.getBlockState(new BlockPos(x,y-m*3,z)).isSolid()){return builder.build();}
        if(!world.getBlockState(new BlockPos(x-1,y-m*1,z-1)).isSolid()){return builder.build();}
        if(!world.getBlockState(new BlockPos(x-1,y-m*1,z+1)).isSolid()){return builder.build();}
        if(!world.getBlockState(new BlockPos(x+1,y-m*1,z-1)).isSolid()){return builder.build();}
        if(!world.getBlockState(new BlockPos(x+1,y-m*1,z+1)).isSolid()){return builder.build();}
        if(world.getBlockState(new BlockPos(x-1,y+m*2,z-1)).isSolid()){return builder.build();}
        if(world.getBlockState(new BlockPos(x-1,y+m*2,z+1)).isSolid()){return builder.build();}
        if(world.getBlockState(new BlockPos(x+1,y+m*2,z-1)).isSolid()){return builder.build();}
        if(world.getBlockState(new BlockPos(x+1,y+m*2,z+1)).isSolid()){return builder.build();}
        builder.add(new BlockPos(x,y,z));
        return builder.build();
    }

    @Override
    public PlacementModifierType<?> getType() {
        return (PlacementModifierType<?>) FbasedPlacers.FBASED_A6;
    }
}