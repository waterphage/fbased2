package com.waterphage.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.waterphage.meta.ChunkExtension;
import com.waterphage.meta.ScalableStructure;
import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FossilS extends Feature<FossilS.FossilSConfig> {
    public FossilS(Codec<FossilSConfig> codec) {
        super(codec);
    }
    public static class FossilSConfig implements FeatureConfig {
        public static final Codec<FossilSConfig> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Identifier.CODEC.listOf().fieldOf("str").forGetter(config -> config.str),
                        StructureProcessorType.REGISTRY_CODEC.listOf().fieldOf("proc").forGetter(config -> config.proc),
                        Codec.FLOAT.listOf().fieldOf("scales").forGetter(config -> config.scales),
                        Codec.FLOAT.listOf().fieldOf("weights").forGetter(config -> config.weights)
                ).apply(instance, FossilSConfig::new)
        );

        public final List<Identifier>  str;
        public final List<RegistryEntry<StructureProcessorList>> proc;
        public final List<Float> scales; //0.05f, 0.154f, 0.368f, 0.687f, 1.0f
        public final List<Float> weights;
        public FossilSConfig(
                List<Identifier>  str,
                List<RegistryEntry<StructureProcessorList>> proc,
                List<Float> scales,
                List<Float> weights
        ) {
            this.str=str;
            this.proc = proc;
            this.scales=scales;
            this.weights=weights;
        }
    }
    private static final Map<Identifier, List<StructureTemplate>> CACHE = new HashMap<>();

    private static List<StructureTemplate> buildScaled(
            Identifier id,
            StructureTemplateManager mgr,
            ServerWorld world,
            FossilSConfig c
    ) {
        StructureTemplate base = mgr.getTemplateOrBlank(id);

        List<StructureTemplate> list = new ArrayList<>(c.scales.size());
        for (float scale : c.scales) {
            StructureTemplate copy = cloneTemplate(base, world);
            ((ScalableStructure) copy).scaleStructure(scale);
            list.add(copy);
        }
        return List.copyOf(list); // immutable
    }
    private static StructureTemplate get(
            Identifier id,
            int scaleIndex,
            StructureTemplateManager mgr,
            ServerWorld world,
            FossilSConfig c
    ) {
        return CACHE.computeIfAbsent(id, k ->
                buildScaled(k, mgr, world,c)
        ).get(scaleIndex);
    }
    public static StructureTemplate cloneTemplate(
            StructureTemplate original,
            ServerWorld world
    ) {
        NbtCompound nbt = new NbtCompound();
        original.writeNbt(nbt);

        StructureTemplate copy = new StructureTemplate();
        RegistryEntryLookup<Block> blockLookup =
                world.toServerWorld()
                        .getRegistryManager()
                        .getWrapperOrThrow(RegistryKeys.BLOCK);
        copy.readNbt(blockLookup, nbt);
        return copy;
    }
    public int choose(FossilSConfig c, Random random){
        Float n= random.nextFloat();
        int id=-1;
        for(Float cs:c.weights) {
            id+=1;
            if(n<cs){return id;}
        }
        return 0;
    }
    @Override
    public boolean generate(FeatureContext<FossilSConfig> context) {
        Random random = context.getRandom();
        StructureWorldAccess world = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        FossilSConfig c =context.getConfig();
        ServerWorld serv = world.toServerWorld();
        BlockRotation blockRotation = BlockRotation.random(random);
        StructureTemplateManager structureTemplateManager = world.toServerWorld().getServer().getStructureTemplateManager();
        StructureTemplate work=get(c.str.get(random.nextInt(c.str.size())), choose(c,random), structureTemplateManager,serv,c);
        ChunkPos chunkPos = new ChunkPos(blockPos);
        int cx1=chunkPos.getStartX() - 16;int cz1=chunkPos.getStartZ() - 16;
        int cx2=chunkPos.getEndX() + 16;int cz2=chunkPos.getEndZ() + 16;
        BlockBox blockBox = new BlockBox(
                cx1,
                world.getBottomY(),
                cz1,
                cx2,
                world.getTopY(),
                cz2
        );
        StructurePlacementData structurePlacementData = new StructurePlacementData().setRotation(blockRotation).setBoundingBox(blockBox).setRandom(random);
        Vec3i vec3i = work.getRotatedSize(blockRotation);
        int dx=vec3i.getX()/2;int dz=vec3i.getZ()/2;int dy=vec3i.getY()/2;
        int x=blockPos.getX();int z=blockPos.getZ();
        int x1= x-dx>cx1?0:cx1-x+dx;
        int x2= x+dx<cx2?0:x+dx-cx2;
        int z1= z-dz>cz1?0:cz1-z+dz;
        int z2= z+dz<cz2?0:z+dz-cz2;
        BlockPos blockPos2 = blockPos.add(-dx+x1-x2, -dy, -dz+z1-z2);
        BlockPos blockPos3 = work.offsetByTransformedSize(blockPos2, BlockMirror.NONE, blockRotation);

        structurePlacementData.clearProcessors();
        c.proc.get(random.nextInt(c.proc.size())).value().getList().forEach(structurePlacementData::addProcessor);
        work.place(world, blockPos3, blockPos3, structurePlacementData, random, 4);
        return true;
    }
}
