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
        Vec3i vec3i = work.getRotatedSize(blockRotation);
        int x=blockPos.getX();int z=blockPos.getZ();int y=blockPos.getY();
        int vx=vec3i.getX()/2;int vz=vec3i.getZ()/2;int vy=vec3i.getY()/2;
        int dx=Math.max(0,chunkPos.getStartX()-16-x+vx)-
                Math.max(0,x+vx-chunkPos.getEndX()-16);
        int dz=Math.max(0,chunkPos.getStartZ()-16-z+vz)-
                Math.max(0,z+vz-chunkPos.getEndZ()-16);
        int dy=Math.max(0,world.getBottomY()-y+vy)-
                Math.max(0,blockPos.getY()+vy-world.getTopY());
        int cx1=x-vx+dx;int cx2=x+vx+dx;
        int cz1=z-vz+dz;int cz2=z+vz+dz;
        int cy1=y-vy+dy;int cy2=y+vy+dy;
        BlockBox blockBox = new BlockBox(
                cx1,
                cy1,
                cz1,
                cx2,
                cy2,
                cz2
        );
        StructurePlacementData structurePlacementData = new StructurePlacementData().setRotation(blockRotation).setBoundingBox(blockBox).setRandom(random);
        BlockPos blockPos2 = blockBox.getCenter().add(-vx,-vy,-vz);
        BlockPos blockPos3 = work.offsetByTransformedSize(blockPos2, BlockMirror.NONE, blockRotation);

        structurePlacementData.clearProcessors();
        c.proc.get(random.nextInt(c.proc.size())).value().getList().forEach(structurePlacementData::addProcessor);
        work.place(world, blockPos3, blockPos3, structurePlacementData, random, 4);
        return true;
    }
}
