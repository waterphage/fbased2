package com.waterphage.block.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.List;

public class Fossil extends Feature<Fossil.FossilConfig> {
    public Fossil(Codec<FossilConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<FossilConfig> context) {
        Random random = context.getRandom();
        StructureWorldAccess structureWorldAccess = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        BlockRotation blockRotation = BlockRotation.random(random);
        FossilConfig fossilConfig = context.getConfig();
        int i = random.nextInt(fossilConfig.fossilStructures.size());
        StructureTemplateManager structureTemplateManager = structureWorldAccess.toServerWorld().getServer().getStructureTemplateManager();
        StructureTemplate structureTemplate = structureTemplateManager.getTemplateOrBlank((Identifier)fossilConfig.fossilStructures.get(i));
        ChunkPos chunkPos = new ChunkPos(blockPos);
        BlockBox blockBox = new BlockBox(
                chunkPos.getStartX() - 16,
                structureWorldAccess.getBottomY(),
                chunkPos.getStartZ() - 16,
                chunkPos.getEndX() + 16,
                structureWorldAccess.getTopY(),
                chunkPos.getEndZ() + 16
        );
        StructurePlacementData structurePlacementData = new StructurePlacementData().setRotation(blockRotation).setBoundingBox(blockBox).setRandom(random);
        Vec3i vec3i = structureTemplate.getRotatedSize(blockRotation);
        BlockPos blockPos2 = blockPos.add(-vec3i.getX() / 2, -vec3i.getY() / 2, -vec3i.getZ() / 2);
        BlockPos blockPos3 = structureTemplate.offsetByTransformedSize(blockPos2, BlockMirror.NONE, blockRotation);

        structurePlacementData.clearProcessors();
        fossilConfig.fossilProcessors.value().getList().forEach(structurePlacementData::addProcessor);
        structureTemplate.place(structureWorldAccess, blockPos3, blockPos3, structurePlacementData, random, 4);
        return true;
    }

    public static class FossilConfig implements FeatureConfig {
        public static final Codec<FossilConfig> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                Identifier.CODEC.listOf().fieldOf("structures").forGetter(config -> config.fossilStructures),
                                StructureProcessorType.REGISTRY_CODEC.fieldOf("processors").forGetter(config -> config.fossilProcessors)
                        )
                        .apply(instance, FossilConfig::new)
        );
        public final List<Identifier> fossilStructures;
        public final RegistryEntry<StructureProcessorList> fossilProcessors;

        public FossilConfig(
                List<Identifier> fossilStructures,
                RegistryEntry<StructureProcessorList> fossilProcessors
        ) {
            this.fossilStructures = fossilStructures;
            this.fossilProcessors = fossilProcessors;
        }
    }
}
