package com.waterphage.block;

import com.waterphage.Fbased;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
public class ModItems {

    public static final Item TEST = registerItem("test", new Item(new FabricItemSettings()));
    public static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(Fbased.MOD_ID, name), item);
    }

    private static void addItemsToIngridientItemGroup(FabricItemGroupEntries entries) {

    }

    public static void registerModItems(){

}
}
