package com.waterphage.worldgen.placers;

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
        int xo = world.getChunk(pos).getPos().getStartX();
        int zo = world.getChunk(pos).getPos().getStartZ();
        int yo=0;
        try {
            yo = Integer.parseInt(map);
        } catch (NumberFormatException nfe) {
            for (int j = 0; j < 16; ++j) {
                int xn = xo+j;
                for (int k = 0; k < 16; ++k) {
                    int zn = zo+k;
                    Heightmap.Type rule = Heightmap.Type.valueOf(map);
                    yo = Math.max(yo,(world.getTopY(rule, xn, zn) - 1));
                }
            }
        }

        @FunctionalInterface
        interface MeshChecker {
            boolean check(int xn, int zn, int shx, int shz,int yo2, int i, int sp, int sp2);
        }
        MeshChecker check;
        switch (mesh) {
            case "square":
                check = (xn, zn, shx, shz, yo2, i, sp, sp2) ->
                        check(xn+shx, sp) && check(zn+shz, sp)&&check(yo2-i,sp2);
                break;
            case "romb":
                check = (xn, zn, shx, shz, yo2, i, sp, sp2) ->
                        check(xn+shx + zn+shz, sp) && check(xn+shx - zn-shz, sp)&&check(yo2-i,sp2);
                break;
            case "square_c":
                check = (xn, zn, shx, shz, yo2, i, sp, sp2) ->
                        (check(xn+shx, sp) && check(zn+shz, sp)) || (check((xn+shx + sp), sp) && check((zn+shz + sp), sp))&&check(yo2-i,sp2);
                break;
            case "hex":
                check = (xn, zn, shx, shz, yo2, i, sp, sp2) ->
                        (check2(xn+shx, sp) && check(zn+shz, Math.round(sp/2))) || (check2(xn+shx + sp, sp) && check((zn+shz + Math.round(sp/2)), Math.round(sp/2)))&&check(yo2-i,sp2);
                break;
            case "hex_a":
                check = (xn, zn, shx, shz, yo2, i, sp, sp2) ->
                        (check2(zn+shz, sp) && check(xn+shx, Math.round(sp/2))) || (check2(zn+shz + sp, sp) && check((xn+shx + Math.round(sp/2)), Math.round(sp/2)))&&check(yo2-i,sp2);
                break;
            default:
                check = (xn, zn, shx, shz, yo2, i, sp, sp2) -> true;
                break;
        }

        int sp = spacing;
        int sp2 = spacing2;
        for (int i=yo;i>=world.getBottomY();--i){
            int shx=Math.round(shift.get(0)*i);
            int shz=Math.round(shift.get(1)*i);;
            for (int j = 0; j < 16; ++j) {
                int xn = xo+j;
                for (int k = 0; k < 16; ++k) {
                    int zn = zo+k;
                    if(check.check(xn,zn,shx,shz,yo,i,sp,sp2)){builder.add(new BlockPos(xn,yo-i,zn));}
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
