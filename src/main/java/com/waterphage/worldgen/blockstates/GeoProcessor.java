package com.waterphage.worldgen.blockstates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Map;

import com.mojang.serialization.codecs.UnboundedMapCodec;
import com.waterphage.worldgen.RegDump;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import org.jetbrains.annotations.Nullable;

public class GeoProcessor extends StructureProcessor {
    public static final UnboundedMapCodec<Identifier, List<Identifier>> FB_PROC_CODEC = Codec.unboundedMap(Identifier.CODEC, Identifier.CODEC.listOf());
    public static final Codec<GeoProcessor> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(
                FB_PROC_CODEC.fieldOf("types").forGetter(p -> p.variants)
                ).apply(instance, GeoProcessor::new);});
    private final Map<Identifier, List<Identifier>> variants;
    public GeoProcessor(Map<Identifier, List<Identifier>> variants) {
        this.variants = variants;
    }
    @Nullable
    public StructureTemplate.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos pivot, StructureTemplate.StructureBlockInfo originalBlockInfo, StructureTemplate.StructureBlockInfo currentBlockInfo, StructurePlacementData data) {
        Random random = data.getRandom(currentBlockInfo.pos());
        BlockState b=originalBlockInfo.state();
        Identifier id = Registries.BLOCK.getId(b.getBlock());
        List<Identifier> pool = variants.get(id);
        if (pool == null) return currentBlockInfo;
        BlockState nb=Registries.BLOCK.get(pool.get(random.nextInt(pool.size()))).getDefaultState();
        return new StructureTemplate.StructureBlockInfo(currentBlockInfo.pos(),nb,currentBlockInfo.nbt());
    }


    protected StructureProcessorType<?> getType() {
        return GeoPType.GEOP;
    }
}
