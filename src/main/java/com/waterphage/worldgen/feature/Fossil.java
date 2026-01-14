package com.waterphage.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.waterphage.meta.ChunkExtension;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.structure.Structure;

import java.util.List;
import java.util.Map;

public class Fossil extends Feature<Fossil.FossilConfig> {
    public Fossil(Codec<FossilConfig> codec) {
        super(codec);
    }
    public static class FossilConfig implements FeatureConfig {
        public record FullStruct(List<Identifier>  str, RegistryEntry<StructureProcessorList> proc) {}
        public static final Codec<FullStruct> FB_STRUC_CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Identifier.CODEC.listOf().fieldOf("str").forGetter(FullStruct::str),
                        StructureProcessorType.REGISTRY_CODEC.fieldOf("proc").forGetter(FullStruct::proc)
                ).apply(instance, FullStruct::new)
        );
        public static final Codec<FossilConfig> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Codec.INT.listOf().fieldOf("matrix").forGetter(config -> config.matrix),
                        FB_STRUC_CODEC.listOf().fieldOf("geology").forGetter(config -> config.type)
                ).apply(instance, FossilConfig::new)
        );
        public final List<Integer> matrix;
        public final List<FullStruct> type;
        public FossilConfig(
                List<Integer>matrix,
                List<FullStruct> fossilStructures
        ) {
            this.matrix=matrix;
            this.type = fossilStructures;
        }
    }
    @Override
    public boolean generate(FeatureContext<FossilConfig> context) {
        Random random = context.getRandom();
        StructureWorldAccess world = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        FossilConfig c =context.getConfig();
        Chunk chunk= world.getChunk(blockPos);
        if(!(chunk instanceof ChunkExtension ext))return false;
        List<Double> geo=ext.getNoise();
        Double bx= (0.5D*geo.get(0))+1;Double bz= (0.5D*geo.get(1))+1;
        Integer index=Math.toIntExact(Math.round(bx*(c.matrix.get(0)-1)))+c.matrix.get(0)*Math.toIntExact(Math.round(bz*(c.matrix.get(1)-1)));
        if (index < 0 || index >= c.type.size()) return false;
        FossilConfig.FullStruct type=c.type.get(index);
        if(type==null)return false;
        List<Identifier> fossilStructures=type.str;
        RegistryEntry<StructureProcessorList> fossilProcessors=type.proc;


        BlockRotation blockRotation = BlockRotation.random(random);
        int i = random.nextInt(fossilStructures.size());
        StructureTemplateManager structureTemplateManager = world.toServerWorld().getServer().getStructureTemplateManager();
        StructureTemplate structureTemplate = structureTemplateManager.getTemplateOrBlank((Identifier)fossilStructures.get(i));
        ChunkPos chunkPos = new ChunkPos(blockPos);
        BlockBox blockBox = new BlockBox(
                chunkPos.getStartX() - 16,
                world.getBottomY(),
                chunkPos.getStartZ() - 16,
                chunkPos.getEndX() + 16,
                world.getTopY(),
                chunkPos.getEndZ() + 16
        );
        StructurePlacementData structurePlacementData = new StructurePlacementData().setRotation(blockRotation).setBoundingBox(blockBox).setRandom(random);
        Vec3i vec3i = structureTemplate.getRotatedSize(blockRotation);
        int xf=(chunkPos.x-vec3i.getX()/2)>0?blockPos.getX()-vec3i.getX()/2:chunkPos.getStartX();
        int zf=(chunkPos.z-vec3i.getZ()/2)>0?blockPos.getZ()-vec3i.getZ()/2:chunkPos.getStartZ();
        BlockPos blockPos2 = new BlockPos(xf, blockPos.getY()-vec3i.getY() / 2,zf);
        BlockPos blockPos3 = structureTemplate.offsetByTransformedSize(blockPos2, BlockMirror.NONE, blockRotation);

        structurePlacementData.clearProcessors();
        fossilProcessors.value().getList().forEach(structurePlacementData::addProcessor);
        structureTemplate.place(world, blockPos3, blockPos3, structurePlacementData, random, 4);
        return true;
    }
}
