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
        StructureTemplate structureTemplate2 = structureTemplateManager.getTemplateOrBlank((Identifier)fossilConfig.overlayStructures.get(i));
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
        BlockPos blockPos2 = blockPos.add(-vec3i.getX() / 2, 0, -vec3i.getZ() / 2);
        int j = blockPos.getY();

        for(int k = 0; k < vec3i.getX(); ++k) {
            for(int l = 0; l < vec3i.getZ(); ++l) {
                j = Math.min(j, structureWorldAccess.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, blockPos2.getX() + k, blockPos2.getZ() + l));
            }
        }

        int k = Math.max(j - 15 - random.nextInt(10), structureWorldAccess.getBottomY() + 10);
        BlockPos blockPos3 = structureTemplate.offsetByTransformedSize(blockPos2.withY(k), BlockMirror.NONE, blockRotation);

        structurePlacementData.clearProcessors();
        fossilConfig.fossilProcessors.value().getList().forEach(structurePlacementData::addProcessor);
        structureTemplate.place(structureWorldAccess, blockPos3, blockPos3, structurePlacementData, random, 4);
        structurePlacementData.clearProcessors();
        fossilConfig.overlayProcessors.value().getList().forEach(structurePlacementData::addProcessor);
        structureTemplate2.place(structureWorldAccess, blockPos3, blockPos3, structurePlacementData, random, 4);
        return true;
    }

    public static class FossilConfig implements FeatureConfig {
        public static final Codec<FossilConfig> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                Identifier.CODEC.listOf().fieldOf("fossil_structures").forGetter(config -> config.fossilStructures),
                                Identifier.CODEC.listOf().fieldOf("overlay_structures").forGetter(config -> config.overlayStructures),
                                StructureProcessorType.REGISTRY_CODEC.fieldOf("fossil_processors").forGetter(config -> config.fossilProcessors),
                                StructureProcessorType.REGISTRY_CODEC.fieldOf("overlay_processors").forGetter(config -> config.overlayProcessors)
                        )
                        .apply(instance, FossilConfig::new)
        );
        public final List<Identifier> fossilStructures;
        public final List<Identifier> overlayStructures;
        public final RegistryEntry<StructureProcessorList> fossilProcessors;
        public final RegistryEntry<StructureProcessorList> overlayProcessors;

        public FossilConfig(
                List<Identifier> fossilStructures,
                List<Identifier> overlayStructures,
                RegistryEntry<StructureProcessorList> fossilProcessors,
                RegistryEntry<StructureProcessorList> overlayProcessors
        ) {
            if (fossilStructures.isEmpty()) {
                throw new IllegalArgumentException("Fossil structure lists need at least one entry");
            } else if (fossilStructures.size() != overlayStructures.size()) {
                throw new IllegalArgumentException("Fossil structure lists must be equal lengths");
            } else {
                this.fossilStructures = fossilStructures;
                this.overlayStructures = overlayStructures;
                this.fossilProcessors = fossilProcessors;
                this.overlayProcessors = overlayProcessors;
            }
        }
    }
}
