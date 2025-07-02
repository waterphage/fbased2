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
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.List;

public class FossilS extends Feature<FossilS.FossilSConfig> {
    public FossilS(Codec<FossilSConfig> codec) {
        super(codec);
    }
    public static class FossilSConfig implements FeatureConfig {
        public static final Codec<FossilSConfig> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Identifier.CODEC.listOf().fieldOf("str").forGetter(config -> config.str),
                        StructureProcessorType.REGISTRY_CODEC.fieldOf("proc").forGetter(config -> config.proc)
                ).apply(instance, FossilSConfig::new)
        );
        public final List<Identifier>  str;
        public final RegistryEntry<StructureProcessorList> proc;
        public FossilSConfig(
                List<Identifier>  str,
                RegistryEntry<StructureProcessorList> proc
        ) {
            this.str=str;
            this.proc = proc;
        }
    }
    @Override
    public boolean generate(FeatureContext<FossilSConfig> context) {
        Random random = context.getRandom();
        StructureWorldAccess world = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        FossilSConfig c =context.getConfig();


        BlockRotation blockRotation = BlockRotation.random(random);
        int i = random.nextInt(c.str.size());
        StructureTemplateManager structureTemplateManager = world.toServerWorld().getServer().getStructureTemplateManager();
        StructureTemplate structureTemplate = structureTemplateManager.getTemplateOrBlank((Identifier)c.str.get(i));
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
        BlockPos blockPos2 = blockPos.add(-vec3i.getX() / 2, -vec3i.getY() / 2, -vec3i.getZ() / 2);
        BlockPos blockPos3 = structureTemplate.offsetByTransformedSize(blockPos2, BlockMirror.NONE, blockRotation);

        structurePlacementData.clearProcessors();
        c.proc.value().getList().forEach(structurePlacementData::addProcessor);
        structureTemplate.place(world, blockPos3, blockPos3, structurePlacementData, random, 4);
        return true;
    }
}
