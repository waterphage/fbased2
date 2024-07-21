package com.waterphage.block.worldgen.placers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

import java.util.List;
import java.util.stream.Stream;

public class GeoPlacer extends PlacementModifier {

    public static final Codec<GeoPlacer> MODIFIER_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.INT.fieldOf("spacing").forGetter(geoPlacer -> geoPlacer.spacing),
                            Codec.INT.fieldOf("spacingY").forGetter(geoPlacer -> geoPlacer.spacing2),
                            Codec.STRING.fieldOf("mode").forGetter(geoPlacer -> geoPlacer.map),
                            Codec.FLOAT.listOf().fieldOf("shift").forGetter(geoPlacer -> geoPlacer.shift),
                            Codec.STRING.fieldOf("mesh").forGetter(geoPlacer -> geoPlacer.mesh)
                    )
                    .apply(instance, GeoPlacer::new)
    );
    private final int spacing;
    private final int spacing2;
    private final String map;
    private final List<Float> shift;
    private final String mesh;

    private GeoPlacer(int spacing,int spacing2, String map, List<Float> shift, String mesh) {
        this.spacing = spacing;
        this.spacing2 = spacing2;
        this.map = map;
        this.shift = shift;
        this.mesh = mesh;
    }


    public static GeoPlacer of(int spacing,int spacing2, String map, List<Float> shift, String mesh) {
        return new GeoPlacer(spacing,spacing2, map, shift, mesh);
    }
    public boolean check2(int i, int f) {
        return Math.abs((i) % 2*Math.round(f*Math.pow(0.75F,-0.5F)*0.5F)) < 1;
    }
    public boolean check(int i, int f) {
        return Math.abs((i) % f) < 1;
    }

    @Override
    public Stream<BlockPos> getPositions(FeaturePlacementContext context, Random random, BlockPos pos) {
        StructureWorldAccess world = context.getWorld();
        Stream.Builder<BlockPos> builder = Stream.builder();
        int xo = pos.getX();
        int zo = pos.getZ();
        int yo=0;
        try {
            yo = Integer.parseInt(map);
        } catch (NumberFormatException nfe) {
            for (int j = 0; j < 16; ++j) {
                int xn = xo-j+7;
                for (int k = 0; k < 16; ++k) {
                    int zn = zo-k+7;
                    Heightmap.Type rule = Heightmap.Type.valueOf(map);
                    yo = Math.max(yo,(world.getTopY(rule, xn, zn) - 1));
                }
            }
        }
        for (int i=yo;i>=world.getBottomY();--i){
            int shx=Math.round(shift.get(0)*i);
            int shz=Math.round(shift.get(1)*i);;
            for (int j = 0; j < 16; ++j) {
                int xn = xo-j+7;
                for (int k = 0; k < 16; ++k) {
                    int zn = zo-k+7;
                    Heightmap.Type rule = Heightmap.Type.valueOf(map);
                    int sp = spacing;
                    int sp2 = spacing2;
                    boolean test = false;

                    if (mesh.equals("full")) {
                        test = true;
                    }
                    if (mesh.equals("square")) {
                        test = check(xn+shx, sp) && check(zn+shz, sp)&&check(yo-i,sp2);
                    }
                    if (mesh.equals("romb")) {
                        test = check(xn+shx + zn+shz, sp) && check(xn+shx - zn-shz, sp)&&check(yo-i,sp2);
                    }
                    if (mesh.equals("square_c")) {
                        test = (check(xn+shx, sp) && check(zn+shz, sp)) || (check((xn+shx + sp), sp) && check((zn+shz + sp), sp))&&check(yo-i,sp2);
                    }
                    if (mesh.equals("hex")) {
                        test = (check2(xn+shx, sp) && check(zn+shz, Math.round(sp/2))) || (check2(xn+shx + sp, sp) && check((zn+shz + Math.round(sp/2)), Math.round(sp/2)))&&check(yo-i,sp2);
                    }
                    if (mesh.equals("hex_a")) {
                        test = (check2(zn+shz, sp) && check(xn+shx, Math.round(sp/2))) || (check2(zn+shz + sp, sp) && check((xn+shx + Math.round(sp/2)), Math.round(sp/2)))&&check(yo-i,sp2);
                    }
                    if (test) {
                        builder.add(new BlockPos(xn,yo-i,zn));
                    }
                }
            }
        }
        return builder.build();
    }

    @Override
    public PlacementModifierType<?> getType() {
        return (PlacementModifierType<?>) FbasedPlacers.FBASED_A1;
    }
}
