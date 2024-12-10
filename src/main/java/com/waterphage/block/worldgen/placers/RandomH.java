package com.waterphage.block.worldgen.placers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

import java.util.List;
import java.util.stream.Stream;

public class RandomH extends PlacementModifier {

    public static final Codec<RandomH> MODIFIER_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            IntProvider.createValidatingCodec(0, 256).fieldOf("count").forGetter(Layer -> Layer.count),
                            Codec.STRING.fieldOf("mode").forGetter(Layer -> Layer.map)
                    )
                    .apply(instance, RandomH::new)
    );
    private final IntProvider count;
    private final String map;


    private RandomH(IntProvider count,String map) {
        this.count = count;
        this.map=map;
    }
    public static RandomH of(IntProvider count,String map) {
        return new RandomH(count,map);
    }
    @Override
    public Stream<BlockPos> getPositions(FeaturePlacementContext context, Random random, BlockPos pos) {
        StructureWorldAccess world = context.getWorld();
        Stream.Builder<BlockPos> builder = Stream.builder();
        pos=world.getChunk(pos).getPos().getStartPos();
        @FunctionalInterface
        interface MeshChecker {
            int y(int x, int z);
        }
        int b=world.getBottomY();
        MeshChecker c=(x,z)->0;
        boolean er=false;
        int ys=0;
        String mmp="OCEAN_FLOOR_WG";
        try {
            ys=Integer.parseInt(map);
        } catch (NumberFormatException nfe) {
            er=true;
            mmp=map;
            ys=0;
        }
        if (er){
            Heightmap.Type rule = Heightmap.Type.valueOf(mmp);
            c = (x, z) -> world.getTopY(rule, x, z) - b;
        }else{
            int yss=ys;
            c = (x, z) ->yss-b;
        }
        for (int i=0;i<=count.get(random);i++) {
            int x = pos.getX() + random.nextInt(16);
            int z = pos.getZ() + random.nextInt(16);
            int y=random.nextInt(c.y(x,z))+b;
            builder.add(new BlockPos(x,y,z));
        }
        return builder.build();
    }
    @Override
    public PlacementModifierType<?> getType() {
        return (PlacementModifierType<?>) FbasedPlacers.FBASED_A8;
    }
}
