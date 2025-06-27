package com.waterphage.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.Map;

public class Stone extends Feature<Stone.StoneConfig> {
    public Stone(Codec<StoneConfig> codec) {super(codec);}
    public static class StoneConfig implements FeatureConfig {
        public static final Codec<StoneConfig> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Codec.STRING.fieldOf("type").forGetter(config -> config.type)
                ).apply(instance, StoneConfig::new));
        private String type;
        StoneConfig(String type) {
            this.type=type;
        }
    }
    @Override
    public boolean generate(FeatureContext<StoneConfig> context) {
        int x=context.getOrigin().getX();
        int y=context.getOrigin().getY();
        int z=context.getOrigin().getZ();
        String type=context.getConfig().type;
        BlockPos.Mutable orig=new BlockPos.Mutable(x,y,z);
        StructureWorldAccess world=context.getWorld();
        String id=Registries.BLOCK.getId(world.getBlockState(orig).getBlock()).toString();
        id = id.replace("_raw","_"+type);
        BlockState block=Registries.BLOCK.get(new Identifier(id)).getDefaultState();
        if(block.isAir()){
            y=(int)Math.round(Math.random()*16);
            id=Registries.BLOCK.getId(world.getBlockState(orig.add(0,-y,0)).getBlock()).toString();
            block=Registries.BLOCK.get(new Identifier(id)).getDefaultState();
            if(block.isAir()){
                id="fbased:white_quartzite_"+type;
                block=Registries.BLOCK.get(new Identifier(id)).getDefaultState();}
            else {
                id = id.replace("_raw","_"+type);
                block=Registries.BLOCK.get(new Identifier(id)).getDefaultState();}
        }
        context.getWorld().setBlockState(context.getOrigin(),block,3);
        return true;
    }
}
