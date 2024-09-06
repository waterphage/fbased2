package com.waterphage.block;

import com.waterphage.Fbased;
import com.waterphage.block.models.*;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.enums.Instrument;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.Arrays;


public class ModBlocks {

    private static final Block AI2 = registerBlock("geo_filler_3", new FbBlock(FabricBlockSettings.copyOf(Blocks.STONE).hardness(0.333F*5F).resistance(0.333F*1.25F)));
    private static final Block AI1 = registerBlock("geo_filler_2", new FbBlock(FabricBlockSettings.copyOf(Blocks.STONE).hardness(0.8F*5F).resistance(0.8F*1.25F)));
    private static final Block A0 = registerBlock("geo_filler_1", new FbBlock(FabricBlockSettings.copyOf(Blocks.STONE).hardness(0.767F*5F).resistance(0.767F*1.25F)));
    private static final Block B1 = registerBlock("carcass", new FbCrpBlock(FabricBlockSettings.copyOf(Blocks.OAK_WOOD).nonOpaque().hardness(0).resistance(0)));
    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(Fbased.MOD_ID, name), block);

    }
    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier(Fbased.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    };

    private static Block stone(String name, Float base) {

        registerBlock(name + "_raw", new FbBlock(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).instrument(Instrument.BASEDRUM).hardness(base*5F).resistance(base*1.25F)));
        registerBlock(name + "_pol", new FbBlock(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).instrument(Instrument.BASEDRUM).hardness(base*3F).resistance(base*1.5F)));
        registerBlock(name + "_cobble", new FbBlock(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).instrument(Instrument.BASEDRUM).hardness(base*2F).resistance(base*1.75F)));
        registerBlock(name + "_bricks", new FbBlock(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).instrument(Instrument.BASEDRUM).hardness(base).resistance(base*2F)));

        registerBlock(name + "_raw_wall", new WallBlock(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).instrument(Instrument.BASEDRUM).hardness(base*5F).resistance(base*1.25F)));
        registerBlock(name + "_pol_wall", new WallBlock(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).instrument(Instrument.BASEDRUM).hardness(base*3F).resistance(base*1.5F)));
        registerBlock(name + "_cobble_wall", new WallBlock(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).instrument(Instrument.BASEDRUM).hardness(base*2F).resistance(base*1.75F)));
        registerBlock(name + "_bricks_wall", new WallBlock(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).instrument(Instrument.BASEDRUM).hardness(base).resistance(base*2F)));

        registerBlock(name + "_raw_stairs", new FbStrBlock(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).instrument(Instrument.BASEDRUM).hardness(base*5F).resistance(base*1.25F)));
        registerBlock(name + "_pol_stairs", new FbStrBlock(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).instrument(Instrument.BASEDRUM).hardness(base*3F).resistance(base*1.5F)));
        registerBlock(name + "_cobble_stairs", new FbStrBlock(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).instrument(Instrument.BASEDRUM).hardness(base*2F).resistance(base*1.75F)));
        registerBlock(name + "_bricks_stairs", new FbStrBlock(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).instrument(Instrument.BASEDRUM).hardness(base).resistance(base*2F)));

        registerBlock(name + "_raw_slab", new FbSlbBlock(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).instrument(Instrument.BASEDRUM).hardness(base*5F).resistance(base*1.25F), SoundEvents.BLOCK_STONE_PLACE));
        registerBlock(name + "_pol_slab", new FbSlbBlock(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).instrument(Instrument.BASEDRUM).hardness(base*3F).resistance(base*1.5F), SoundEvents.BLOCK_STONE_PLACE));
        registerBlock(name + "_cobble_slab", new FbSlbBlock(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).instrument(Instrument.BASEDRUM).hardness(base*2F).resistance(base*1.75F), SoundEvents.BLOCK_STONE_PLACE));
        registerBlock(name + "_bricks_slab", new FbSlbBlock(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).instrument(Instrument.BASEDRUM).hardness(base).resistance(base*2F), SoundEvents.BLOCK_STONE_PLACE));

        registerBlock(name + "_gravel", new SandBlock(14406560,FabricBlockSettings.copyOf(Blocks.SAND).hardness(base*0.75F).resistance(base*2.5F)));
        return registerBlock(name + "_dust", new SandBlock(14406560,FabricBlockSettings.copyOf(Blocks.SAND).hardness(base*0.25F).resistance(base*5F)));
    }
    private static String rocks(){
        for (ModMaterials.Rock dir: ModMaterials.Rock.values()){
            for (String s : Arrays.asList(
                    dir.c1+"_"+dir.name,
                    dir.c2+"_"+dir.name,
                    dir.c3+"_"+dir.name,
                    dir.c4+"_"+dir.name
            )){
                stone(s, dir.base);
            }
        }
        return null;
    }

    private static String carcass(){
        for (ModMaterials.Color dir: ModMaterials.Color.values()){
            registerBlock(dir.name + "_carcass", new FbCrpBlock(FabricBlockSettings.create().mapColor(MapColor.SPRUCE_BROWN).instrument(Instrument.BASS).hardness(0).resistance(0).nonOpaque()));
            registerBlock(dir.name + "_carcass_stairs", new FbCrpStrBlock(FabricBlockSettings.create().mapColor(MapColor.SPRUCE_BROWN).instrument(Instrument.BASS).hardness(0).resistance(0).nonOpaque()));
            registerBlock(dir.name + "_carcass_slab", new FbCrpSlbBlock(FabricBlockSettings.create().mapColor(MapColor.SPRUCE_BROWN).instrument(Instrument.BASS).hardness(0).resistance(0).nonOpaque(), SoundEvents.BLOCK_WOOD_PLACE));
        }
        return null;
    }
    private static final String CARCASS = carcass();
    private static final String ROCK = rocks();
    private static String minerals(){
        for (ModMaterials.Mineral dir: ModMaterials.Mineral.values()){
            stone(dir.name, dir.base);
        }
        return null;
    }
    private static final String MINERAL = minerals();
    public static void registerModBlocks() {
        for (ModMaterials.Color dir: ModMaterials.Color.values()){
            String id1="fbased:"+dir.name+"_carcass";
            String id2="fbased:"+dir.name+"_carcass_stairs";
            String id3="fbased:"+dir.name+"_carcass_slab";
            BlockRenderLayerMap.INSTANCE.putBlock(Registries.BLOCK.get(new Identifier(id1)), RenderLayer.getTranslucent());
            BlockRenderLayerMap.INSTANCE.putBlock(Registries.BLOCK.get(new Identifier(id2)), RenderLayer.getTranslucent());
            BlockRenderLayerMap.INSTANCE.putBlock(Registries.BLOCK.get(new Identifier(id3)), RenderLayer.getTranslucent());
        }
    }
}