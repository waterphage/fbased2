package com.waterphage.worldgen.feature;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.*;

public class FossilS extends Feature<FossilS.FossilSConfig> {
    public FossilS(Codec<FossilSConfig> codec) {
        super(codec);
    }
    private static final Map<Identifier, Float> STR_SIZES = Map.ofEntries(
            Map.entry(new Identifier("fbased","pod_1_1"), 0.873033f),
            Map.entry(new Identifier("fbased","pod_1_2"), 0.859366f),
            Map.entry(new Identifier("fbased","pod_1_3"), 0.859415f),
            Map.entry(new Identifier("fbased","pod_1_4"), 0.862269f),
            Map.entry(new Identifier("fbased","pod_1_5"), 0.862021f),
            Map.entry(new Identifier("fbased","pod_1_6"), 0.859512f),
            Map.entry(new Identifier("fbased","pod_1_7"), 0.858245f),
            Map.entry(new Identifier("fbased","pllr_1_1"), 0.740070f),
            Map.entry(new Identifier("fbased","pllr_1_2"), 0.754020f),
            Map.entry(new Identifier("fbased","pllr_1_3"), 0.753962f),
            Map.entry(new Identifier("fbased","pllr_1_4"), 0.753326f),
            Map.entry(new Identifier("fbased","pllr_1_5"), 0.753933f),
            Map.entry(new Identifier("fbased","pllr_1_6"), 0.754194f),
            Map.entry(new Identifier("fbased","pllr_1_7"), 0.740070f),
            Map.entry(new Identifier("fbased","pllr_2_1"), 0.733833f),
            Map.entry(new Identifier("fbased","pllr_2_2"), 0.748371f),
            Map.entry(new Identifier("fbased","pllr_2_3"), 0.736476f),
            Map.entry(new Identifier("fbased","pllr_2_4"), 0.738678f),
            Map.entry(new Identifier("fbased","pllr_2_5"), 0.736450f),
            Map.entry(new Identifier("fbased","pllr_2_6"), 0.747949f),
            Map.entry(new Identifier("fbased","pllr_2_7"), 0.733833f),
            Map.entry(new Identifier("fbased","acub_1_1"), 0.759817f),
            Map.entry(new Identifier("fbased","acub_1_2"), 0.778435f),
            Map.entry(new Identifier("fbased","acub_1_3"), 0.782039f),
            Map.entry(new Identifier("fbased","acub_1_4"), 0.785926f),
            Map.entry(new Identifier("fbased","acub_1_5"), 0.779921f),
            Map.entry(new Identifier("fbased","acub_1_6"), 0.777436f),
            Map.entry(new Identifier("fbased","acub_1_7"), 0.784097f),
            Map.entry(new Identifier("fbased","cube_1_1"), 0.755701f),
            Map.entry(new Identifier("fbased","cube_1_2"), 0.716010f),
            Map.entry(new Identifier("fbased","cube_1_3"), 0.713761f),
            Map.entry(new Identifier("fbased","cube_1_4"), 0.712701f),
            Map.entry(new Identifier("fbased","cube_1_5"), 0.715027f),
            Map.entry(new Identifier("fbased","cube_1_6"), 0.711902f),
            Map.entry(new Identifier("fbased","cube_1_7"), 0.710906f),
            Map.entry(new Identifier("fbased","disc_1_1"), 0.885533f),
            Map.entry(new Identifier("fbased","disc_1_2"), 0.891208f),
            Map.entry(new Identifier("fbased","disc_1_3"), 0.892817f),
            Map.entry(new Identifier("fbased","disc_1_4"), 0.895685f),
            Map.entry(new Identifier("fbased","disc_1_5"), 0.887606f),
            Map.entry(new Identifier("fbased","disc_1_6"), 0.881490f),
            Map.entry(new Identifier("fbased","disc_1_7"), 0.884782f),
            Map.entry(new Identifier("fbased","disc_2_1"), 0.878201f),
            Map.entry(new Identifier("fbased","disc_2_2"), 0.888807f),
            Map.entry(new Identifier("fbased","disc_2_3"), 0.894698f),
            Map.entry(new Identifier("fbased","disc_2_4"), 0.890309f),
            Map.entry(new Identifier("fbased","disc_2_5"), 0.877809f),
            Map.entry(new Identifier("fbased","disc_2_6"), 0.867780f),
            Map.entry(new Identifier("fbased","disc_2_7"), 0.881027f),
            Map.entry(new Identifier("fbased","disc_3_1"), 0.868386f),
            Map.entry(new Identifier("fbased","disc_3_2"), 0.886557f),
            Map.entry(new Identifier("fbased","disc_3_3"), 0.876287f),
            Map.entry(new Identifier("fbased","disc_3_4"), 0.874693f),
            Map.entry(new Identifier("fbased","disc_3_5"), 0.866389f),
            Map.entry(new Identifier("fbased","disc_3_6"), 0.859082f),
            Map.entry(new Identifier("fbased","disc_3_7"), 0.869495f),
            Map.entry(new Identifier("fbased","disc_4_1"), 0.897723f),
            Map.entry(new Identifier("fbased","disc_4_2"), 0.904917f),
            Map.entry(new Identifier("fbased","disc_4_3"), 0.901649f),
            Map.entry(new Identifier("fbased","disc_4_4"), 0.901929f),
            Map.entry(new Identifier("fbased","disc_4_5"), 0.901410f),
            Map.entry(new Identifier("fbased","disc_4_6"), 0.896292f),
            Map.entry(new Identifier("fbased","disc_4_7"), 0.896160f),
            Map.entry(new Identifier("fbased","disc_5_1"), 0.883526f),
            Map.entry(new Identifier("fbased","disc_5_2"), 0.901007f),
            Map.entry(new Identifier("fbased","disc_5_3"), 0.904150f),
            Map.entry(new Identifier("fbased","disc_5_4"), 0.908746f),
            Map.entry(new Identifier("fbased","disc_5_5"), 0.901441f),
            Map.entry(new Identifier("fbased","disc_5_6"), 0.895938f),
            Map.entry(new Identifier("fbased","disc_5_7"), 0.882107f),
            Map.entry(new Identifier("fbased","disc_6_1"), 0.886906f),
            Map.entry(new Identifier("fbased","disc_6_2"), 0.888064f),
            Map.entry(new Identifier("fbased","disc_6_3"), 0.891961f),
            Map.entry(new Identifier("fbased","disc_6_4"), 0.893136f),
            Map.entry(new Identifier("fbased","disc_6_5"), 0.890280f),
            Map.entry(new Identifier("fbased","disc_6_6"), 0.882972f),
            Map.entry(new Identifier("fbased","disc_6_7"), 0.887859f),
            Map.entry(new Identifier("fbased","disc_7_1"), 0.875199f),
            Map.entry(new Identifier("fbased","disc_7_2"), 0.873951f),
            Map.entry(new Identifier("fbased","disc_7_3"), 0.878883f),
            Map.entry(new Identifier("fbased","disc_7_4"), 0.878883f),
            Map.entry(new Identifier("fbased","disc_7_5"), 0.876343f),
            Map.entry(new Identifier("fbased","disc_7_6"), 0.868645f),
            Map.entry(new Identifier("fbased","disc_7_7"), 0.874262f),
            Map.entry(new Identifier("fbased","disc_8_1"), 0.883583f),
            Map.entry(new Identifier("fbased","disc_8_2"), 0.895726f),
            Map.entry(new Identifier("fbased","disc_8_3"), 0.902656f),
            Map.entry(new Identifier("fbased","disc_8_4"), 0.900831f),
            Map.entry(new Identifier("fbased","disc_8_5"), 0.894357f),
            Map.entry(new Identifier("fbased","disc_8_6"), 0.889994f),
            Map.entry(new Identifier("fbased","disc_8_7"), 0.879633f),
            Map.entry(new Identifier("fbased","dode_1_1"), 0.614382f),
            Map.entry(new Identifier("fbased","dode_1_2"), 0.612926f),
            Map.entry(new Identifier("fbased","dode_1_3"), 0.611887f),
            Map.entry(new Identifier("fbased","dode_1_4"), 0.612983f),
            Map.entry(new Identifier("fbased","dode_1_5"), 0.613407f),
            Map.entry(new Identifier("fbased","dode_1_6"), 0.612559f),
            Map.entry(new Identifier("fbased","dode_1_7"), 0.612658f),
            Map.entry(new Identifier("fbased","icos_1_1"), 0.581709f),
            Map.entry(new Identifier("fbased","icos_1_2"), 0.582287f),
            Map.entry(new Identifier("fbased","icos_1_3"), 0.576951f),
            Map.entry(new Identifier("fbased","icos_1_4"), 0.577428f),
            Map.entry(new Identifier("fbased","icos_1_5"), 0.578333f),
            Map.entry(new Identifier("fbased","icos_1_6"), 0.614458f),
            Map.entry(new Identifier("fbased","icos_1_7"), 0.613424f)
    );
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
                List<Identifier> str,
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
        Float stype=STR_SIZES.getOrDefault(id, 1.0f);
        List<StructureTemplate> list = new ArrayList<>(c.scales.size());
        for (float scale : c.scales) {
            StructureTemplate copy = cloneTemplate(base, world);
            ((ScalableStructure) copy).scaleStructure(scale*stype);
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
