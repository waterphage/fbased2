package com.waterphage.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class OreS extends Feature<OreS.OreSConfig> {
    public OreS(Codec<OreSConfig> codec) {
        super(codec);
    }
    @Override
    public boolean generate(FeatureContext<OreSConfig> context) {
        OreSConfig config = context.getConfig();
        BlockPos pos = context.getOrigin();
        StructureWorldAccess world = context.getWorld();
        Random random = context.getRandom();
        ChunkGenerator chunkGenerator = context.getGenerator();
        Identifier probe= Registries.BLOCK.getId(world.getBlockState(pos).getBlock());

        int id = -1;
        for (int i = 0; i < config.blocks.size(); i++) {
            if (config.blocks.get(i).get(0).equals(probe)) {
                id = i;
                break;
            }
        }
        if (id != -1) {
            BlockState goal = Registries.BLOCK.get(config.blocks.get(id).get(1)).getDefaultState();
            world.setBlockState(pos, goal, 2);
            return true;
        }
        return false;
    }

    public static class OreSConfig implements FeatureConfig {
        public static final Codec<OreSConfig> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Identifier.CODEC.listOf().listOf().fieldOf("variants").forGetter(config -> config.blocks)
                ).apply(instance, OreSConfig::new));

        public final List<List<Identifier>> blocks;
        public OreSConfig(List<List<Identifier>> blocks) {
            this.blocks = blocks;
        }
    }
}
