package com.waterphage.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ModMaterials {
    public enum Gem {
        A1("gem"),
        A2("foggy_gem"),
        A3("dirty_gem");

        public final String name;
        Gem(String name) {
            this.name = name;
        }
    }
    public enum Rock {
        A1("sandstone",0.867F,"black","white","yellow","tan"),
        A2("siltstone",0.867F,"black","white","yellow","tan"),
        A3("mudstone",0.333F,"black","white","yellow","tan"),
        A4("shale",0.4F,"black","white","yellow","red"),
        A5("claystone",0.5F,"black","white","yellow","tan"),
        A6("rock_salt",0.333F,"black","white","yellow","red"),
        A7("limestone",0.4F,"black","white","yellow","red"),
        A8("conglomerate",0.333F,"black","white","yellow","tan"),
        A9("dolomite",0.5F,"black","white","yellow","tan"),
        A10("chert",0.9F,"black","white","yellow","tan"),
        A11("chalk",0.133F,"black","white","yellow","tan"),
        A12("granite",0.933F,"black","white","green","red"),
        A13("diorite",0.767F,"black","white","green","red"),
        A14("gabbro",0.933F,"black","white","green","red"),
        A15("rhyolite",0.8F,"black","white","green","red"),
        A16("basalt",0.767F,"black","white","green","red"),
        A17("andesite",0.933F,"black","white","green","red"),
        A18("dacite",0.28F,"black","white","green","red"),
        A19("obsidian",0.733F,"black","white","green","red"),
        A20("quartzite",1F,"black","white","green","tan"),
        A21("slate",0.403F,"black","white","green","tan"),
        A22("phyllite",0.2F,"black","white","green","red"),
        A23("schist",0.5F,"black","white","green","red"),
        A24("gneiss",0.787F,"black","white","green","red"),
        A25("marble",0.4F,"black","white","green","red")
        ;
        public final String name;
        public final Float base;
        public final String c1;
        public final String c2;
        public final String c3;
        public final String c4;
        ;

        Rock(String name,Float base, String c1, String c2, String c3, String c4) {
            this.name = name;
            this.base = base;
            this.c1 = c1;
            this.c2 = c2;
            this.c3 = c3;
            this.c4 = c4;
        }

    }
    public static Map<String, String[]> climate =new HashMap<String, String[]>(){{
            //put("test",new String[]{"hot","wet","dry","cold","normal"});
        put("tetrahedrite",new String[]{"","malachite","","",""});
        put("limonite",new String[]{"hematite","hematite","","",""});
        put("gypsum",new String[]{"anhydrite","anhydrite","","",""});
        put("brimstone",new String[]{"minecraft:magma_block","","","",""});
        put("hematite",new String[]{"","limonite","","",""});
        put("marcasite",new String[]{"hematite","limonite","","",""});
        put("coal_bituminous",new String[]{"minecraft:magma_block","","","",""});
        put("lignite",new String[]{"minecraft:magma_block","","","",""});
        put("jet",new String[]{"minecraft:magma_block","","","",""});
        put("graphite",new String[]{"magma_block","","","",""});
        put("hematite",new String[]{"","limonite","","",""});
        put("horn_silver",new String[]{"native_silver","","","",""});
        put("kaolinite",new String[]{"bauxite","","","",""});
        put("cinnabar",new String[]{"minecraft:magma_block","","","",""});
        put("realgar",new String[]{"minecraft:magma_block","","","",""});
        put("alabaster",new String[]{"","gypsum","","",""});
        put("malachite",new String[]{"native_copper","","","",""});
        put("selenite",new String[]{"","gypsum","","",""});
        put("satinspar",new String[]{"","gypsum","","",""});
        put("native_copper",new String[]{"","malachite","","",""});
        put("orpiment",new String[]{"minecraft:magma_block","","","",""});
        put("anhydrite",new String[]{"","gypsum","","",""});
        put("bismuthinite",new String[]{"minecraft:magma_block","","","",""});
        put("stibnite",new String[]{"minecraft:magma_block","","","",""});

        put("penitente",new String[]{"fbased:mist","","","","break"});
    }};
    public static void climatizer(BlockState state, ServerWorld world, BlockPos pos, RegistryEntry<Biome> biome){
        String[] roster=climate.getOrDefault(Registries.BLOCK.getId(state.getBlock()).getPath().split("_", 2)[0],new String[]{"","","","",""});
        if(biome.isIn(ModTags.FB_HOT)){replaceM(state,world,pos,roster[0]);return;}
        if(biome.isIn(ModTags.FB_WET)){replaceM(state,world,pos,roster[1]);return;}
        if(biome.isIn(ModTags.FB_DRY)){replaceM(state,world,pos,roster[2]);return;}
        if(biome.isIn(ModTags.FB_COLD)){replaceM(state,world,pos,roster[3]);return;}
        replaceM(state,world,pos,roster[4]);
    }
    public static void replaceM(BlockState state, ServerWorld world, BlockPos pos, String newM){
        if(newM==""){return;}
        if (newM=="break"){world.breakBlock(pos,false);return;}
        Identifier targetId;
        if (newM.contains(":")) {
            targetId = Identifier.tryParse(newM);
            if (targetId == null) return;
            Block target = Registries.BLOCK.get(targetId);
            world.setBlockState(pos, target.getDefaultState(), Block.NOTIFY_ALL);
            return;
        }
        Identifier sourceId = Registries.BLOCK.getId(state.getBlock());
        String sourcePath = sourceId.getPath();
        int separator = sourcePath.indexOf('_');
        if (separator < 0) return;
        String targetPath = newM + sourcePath.substring(separator);
        targetId = new Identifier(sourceId.getNamespace(), targetPath);
        Block target = Registries.BLOCK.get(targetId);
        BlockState replacement = target.getDefaultState();
        for (Property<?> property : state.getProperties()) {
            if (replacement.contains(property)) {
                replacement = copyProperty(state, replacement, property);
            }
        }
        world.setBlockState(pos, replacement, Block.NOTIFY_ALL);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static BlockState copyProperty(
            BlockState source,
            BlockState target,
            Property property
    ) {
        Comparable value = source.get(property);

        if (property.getValues().contains(value)) {
            return target.with(property, value);
        }

        return target;
    }
    public enum Mineral {

        B1("hematite", 0.767F),
        B2("limonite", 0.633F),
        B3("garnierite", 0.367F),
        B4("native_gold", 0.367F),
        B5("native_silver", 0.367F),
        B6("native_copper", 0.367F),
        B7("malachite", 0.5F),
        B8("galena", 0.347F),
        B9("sphalerite", 0.5F),
        B10("cassiterite", 0.867F),
        B11("coal_bituminous", 0.367F),
        B12("lignite", 0.267F),
        B13("native_platinum", 0.567F),
        B14("cinnabar", 0.3F),
        B15("cobaltite", 0.733F),
        B16("tetrahedrite", 0.5F),
        B17("horn_silver", 0.2F),
        B18("gypsum", 0.233F),
        B19("talc", 0.133F),
        B20("jet", 0.433F),
        B21("puddingstone", 0.967F),
        B22("graphite", 0.2F),
        B22I("petrified_wood", 1F),
        B23("brimstone", 0.267F),
        B24("kimberlite", 0.867F),
        B25("bismuthinite", 0.3F),
        B26("realgar", 0.233F),
        B27("orpiment", 0.233F),
        B28("stibnite", 0.267F),
        B29("marcasite", 0.833F),
        B30("sylvite", 0.333F),
        B31("cryolite", 0.367F),
        B32("periclase", 0.767F),
        B33("ilmenite", 0.773F),
        B34("rutile", 0.833F),
        B35("magnetite", 0.8F),
        B36("chromite", 0.733F),
        B37("pyrolusite", 0.833F),
        B38("pitchblende", 0.733F),
        B39("bauxite", 0.3F),
        B40("native_aluminum", 0.367F),
        B41("borax", 0.3F),
        B42("olivine", 0.9F),
        B43("hornblende", 0.733F),
        B44("kaolinite", 0.3F),
        B45("serpentine", 0.6F),
        B46("orthoclase", 0.8F),
        B47("microcline", 0.833F),
        B48("mica", 0.433F),
        B49("calcite", 0.4F),
        B50("saltpeter", 0.233F),
        B51("alabaster", 0.233F),
        B52("selenite", 0.267F),
        B53("satinspar", 0.267F),
        B54("anhydrite", 0.467F),
        B55("alunite", 0.533F);
        public final String name;
        public final Float base;
        public final Boolean gem;
        public final Integer transp;


        Mineral(String name,Float base,Boolean gem,Integer transp) {
            this.name = name;
            this.base = base;
            this.gem = gem;
            this.transp = transp;
        }
        Mineral(String name,Float base,Boolean gem) {
            this.name = name;
            this.base = base;
            this.gem = gem;
            this.transp = 0;
        }
        Mineral(String name,Float base) {
            this.name = name;
            this.base = base;
            this.gem = false;
            this.transp = 0;
        }
    }

    public enum Color {

        C1("white",0),
        C2("light_gray",4),
        C3("gray",5),
        C4("black",8),
        C5("brown",6),
        C6("red",5),
        C7("orange",4),
        C8("yellow",3),
        C9("lime",4),
        C10("green",5),
        C11("cyan",4),
        C12("light_blue",3),
        C13("blue",5),
        C14("purple",3),
        C15("magenta",4),
        C16("pink",2)
        ;
        public final String name;
        public final int l;

        Color(String name,int l) {this.name = name;this.l=l;}

    }
    public static void registerModMaterials(){

    }
}
