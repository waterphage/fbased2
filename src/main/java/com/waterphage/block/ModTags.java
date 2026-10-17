package com.waterphage.block;

import com.waterphage.Fbased;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModTags {

    public static final TagKey<Block> FBTINT = TagKey.of(RegistryKeys.BLOCK, new Identifier(Fbased.MOD_ID, "tint"));
    public static final TagKey<Item> FBBLOCKS = TagKey.of(RegistryKeys.ITEM, new Identifier(Fbased.MOD_ID, "fb_blocks"));
    public static final TagKey<Item> FBSTAIRS = TagKey.of(RegistryKeys.ITEM, new Identifier(Fbased.MOD_ID, "fb_stairs"));
    public static final TagKey<Item> FBSLABS = TagKey.of(RegistryKeys.ITEM, new Identifier(Fbased.MOD_ID, "fb_slabs"));
    public static void registerModTags() {

    }
}
