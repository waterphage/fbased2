package com.waterphage.worldgen.blockstates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import com.waterphage.worldgen.ModRules;
import io.netty.util.internal.MathUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class RadProcessor extends StructureProcessor {
    public static final Codec<RadProcessor> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(
                Identifier.CODEC.listOf().fieldOf("types").forGetter(p -> p.variants),
                Codec.INT.listOf().fieldOf("modes").forGetter(p->p.modes),
                Codec.DOUBLE.listOf().fieldOf("amp").forGetter(p->p.amp)
                ).apply(instance, RadProcessor::new);});
    private final List<Identifier> variants;
    private final List<Integer> modes;
    private final List<Double> amp;
    public static final TagKey<Block> STR = TagKey.of(RegistryKeys.BLOCK, new Identifier("fbased", "str_all"));
    public RadProcessor(List<Identifier> variants, List<Integer> modes,List<Double> amp) {
        this.variants = variants;
        this.modes=modes;
        this.amp=amp;
    }
    public Double ф(Double dx,Double dy){
        double a = Math.atan2(dy, dx);
        if (a < 0) a += Math.PI * 2;
        return a;
    }
    @Nullable
    public StructureTemplate.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos pivot, StructureTemplate.StructureBlockInfo originalBlockInfo, StructureTemplate.StructureBlockInfo currentBlockInfo, StructurePlacementData data) {
        if(!currentBlockInfo.state().isIn(STR)){return currentBlockInfo;}
        BlockPos org=data.getBoundingBox().getCenter();
        BlockPos loc=currentBlockInfo.pos();
        long s = org.asLong()^((org.getX()*31L) + (org.getZ()*17L)); double d=2D*Math.PI;
        int dx=(loc.getX()-org.getX());
        int dy=(loc.getY()-org.getY());
        int dz=(loc.getZ()-org.getZ());
        double rad= Math.sqrt(dx*dx+dy*dy+dz*dz);
        Random r = new Random(s^(3*0x9E3779B97F4A7C15L));
        double ф1=Math.atan2(dy,dx)+d*r.nextDouble();
        r.setSeed(s^(2*0x9E3779B97F4A7C15L));
        Double ф2=Math.atan2(dy, Math.sqrt(dx*dx + dz*dz))+d*r.nextDouble();
        double mag=0D;
        for (int i=0;i<modes.size();++i){
            double m=modes.get(i);
            double a=amp.get(i);
            mag+= Math.sin(ф1*m)*Math.sin(ф2*m)*a;
        }
        r.setSeed(s^(Math.round(rad+mag)*0x9E3779B97F4A7C15L));
        Identifier id= variants.get(r.nextInt(variants.size()));
        BlockState nb=Registries.BLOCK.get(id).getDefaultState();
        return new StructureTemplate.StructureBlockInfo(currentBlockInfo.pos(),nb,currentBlockInfo.nbt());
    }
    protected StructureProcessorType<?> getType() {
        return GeoPType.GEOR;
    }
}
