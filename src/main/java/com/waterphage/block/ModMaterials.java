package com.waterphage.block;

public class ModMaterials {
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
        B55("alunite", 0.533F)
                ;
        public final String name;
        public final Float base;
        ;

        Mineral(String name,Float base) {
            this.name = name;
            this.base = base;
        }

    }

    public enum Color {

        C1("white"),
        C2("light_gray"),
        C3("gray"),
        C4("black"),
        C5("brown"),
        C6("red"),
        C7("orange"),
        C8("yellow"),
        C9("lime"),
        C10("green"),
        C11("cyan"),
        C12("light_blue"),
        C13("blue"),
        C14("purple"),
        C15("magenta"),
        C16("pink")
        ;
        public final String name;

        Color(String name) {
            this.name = name;
        }

    }
    public static void registerModMaterials(){

    }
}
