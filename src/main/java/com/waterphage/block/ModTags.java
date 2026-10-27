package com.waterphage.block;

import com.waterphage.Fbased;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

public class ModTags {
    public static final TagKey<Fluid> FBFLOW= TagKey.of(RegistryKeys.FLUID, new Identifier(Fbased.MOD_ID, "flowing"));
    public static final TagKey<Block> FBSPIKE= TagKey.of(RegistryKeys.BLOCK, new Identifier(Fbased.MOD_ID, "spike"));

    public static final TagKey<Block> FBTINT = TagKey.of(RegistryKeys.BLOCK, new Identifier(Fbased.MOD_ID, "tint"));
    public static final TagKey<Item> FBBLOCKS = TagKey.of(RegistryKeys.ITEM, new Identifier(Fbased.MOD_ID, "fb_blocks"));
    public static final TagKey<Item> FBSTAIRS = TagKey.of(RegistryKeys.ITEM, new Identifier(Fbased.MOD_ID, "fb_stairs"));
    public static final TagKey<Item> FBSLABS = TagKey.of(RegistryKeys.ITEM, new Identifier(Fbased.MOD_ID, "fb_slabs"));
    public static final TagKey<Biome> FB_HOT =
            TagKey.of(RegistryKeys.BIOME, new Identifier("fbased", "hot"));

    public static final TagKey<Biome> FB_COLD =
            TagKey.of(RegistryKeys.BIOME, new Identifier("fbased", "cold"));

    public static final TagKey<Biome> FB_DRY =
            TagKey.of(RegistryKeys.BIOME, new Identifier("fbased", "dry"));

    public static final TagKey<Biome> FB_WET =
            TagKey.of(RegistryKeys.BIOME, new Identifier("fbased", "wet"));
    public static void registerModTags() {

    }
}
