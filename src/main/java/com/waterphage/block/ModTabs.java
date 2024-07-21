package com.waterphage.block;

import com.waterphage.Fbased;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Arrays;

public class ModTabs {
    public static final ItemGroup RUBY_GROUP = Registry.register(Registries.ITEM_GROUP,
            new Identifier(Fbased.MOD_ID, "stones"),
            FabricItemGroup.builder().displayName(Text.translatable("fbased.itemgroup.stones"))
                    .icon(() -> new ItemStack(Registries.ITEM.get(new Identifier("fbased:black_dacite_raw"))))
                    .entries((displayContext, entries) -> {

                        for (ModMaterials.Rock dir: ModMaterials.Rock.values()){
                            for (String s : Arrays.asList(
                                    "fbased:"+dir.c1+"_"+dir.name,
                                    "fbased:"+dir.c2+"_"+dir.name,
                                    "fbased:"+dir.c3+"_"+dir.name,
                                    "fbased:"+dir.c4+"_"+dir.name
                            )) {
                                for (String d : Arrays.asList(
                                        s+"_raw",
                                        s+"_pol",
                                        s+"_cobble",
                                        s+"_bricks"
                                ))
                                {
                                    for (String e : Arrays.asList(
                                            d,
                                            d+"_stairs",
                                            d+"_slab",
                                            d+"_wall"
                                    ))
                                    {
                                        entries.add(new ItemStack(Registries.ITEM.get(new Identifier(e))));
                                    }
                                }
                                String gravel = s+"_gravel";
                                String dust = s+"_dust";
                                entries.add(new ItemStack(Registries.ITEM.get(new Identifier(gravel))));
                                entries.add(new ItemStack(Registries.ITEM.get(new Identifier(dust))));
                            }
                        }

                    }).build());

    public static final ItemGroup MINERALS = Registry.register(Registries.ITEM_GROUP,
            new Identifier(Fbased.MOD_ID, "minerals"),
            FabricItemGroup.builder().displayName(Text.translatable("fbased.itemgroup.minerals"))
                    .icon(() -> new ItemStack(Registries.ITEM.get(new Identifier("fbased:hematite_raw"))))
                    .entries((displayContext, entries) -> {

                        for (ModMaterials.Mineral dir: ModMaterials.Mineral.values()){
                            for (String s : Arrays.asList(
                                    "fbased:"+dir.name
                            )) {
                                for (String d : Arrays.asList(
                                        s+"_raw",
                                        s+"_pol",
                                        s+"_cobble",
                                        s+"_bricks"
                                ))
                                {
                                    for (String e : Arrays.asList(
                                            d,
                                            d+"_stairs",
                                            d+"_slab",
                                            d+"_wall"
                                    ))
                                    {
                                        entries.add(new ItemStack(Registries.ITEM.get(new Identifier(e))));
                                    }
                                }
                                String gravel = s+"_gravel";
                                String dust = s+"_dust";
                                entries.add(new ItemStack(Registries.ITEM.get(new Identifier(gravel))));
                                entries.add(new ItemStack(Registries.ITEM.get(new Identifier(dust))));
                            }
                        }

                    }).build());

    public static final ItemGroup MISC = Registry.register(Registries.ITEM_GROUP,
            new Identifier(Fbased.MOD_ID, "miscellaneous"),
            FabricItemGroup.builder().displayName(Text.translatable("fbased.itemgroup.misc"))
                    .icon(() -> new ItemStack(Registries.ITEM.get(new Identifier("fbased:carcass"))))
                    .entries((displayContext, entries) -> {
                        for (ModMaterials.Color dir: ModMaterials.Color.values()){
                            String id1="fbased:"+dir.name+"_carcass";
                            String id2="fbased:"+dir.name+"_carcass_stairs";
                            String id3="fbased:"+dir.name+"_carcass_slab";
                            entries.add(new ItemStack(Registries.ITEM.get(new Identifier(id1))));
                            entries.add(new ItemStack(Registries.ITEM.get(new Identifier(id2))));
                            entries.add(new ItemStack(Registries.ITEM.get(new Identifier(id3))));
                        }
                        entries.add(new ItemStack(Registries.ITEM.get(new Identifier("fbased:carcass"))));
                    }).build());

    private ModTabs() {}
    public static void registerModTabs(){

    }
}
