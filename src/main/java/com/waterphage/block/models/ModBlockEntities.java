package com.waterphage.block.models;

import com.waterphage.Fbased;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import com.waterphage.block.ModBlocks;

public class ModBlockEntities {
    // Объявляем тип вашего BlockEntity
    public static BlockEntityType<TechBlockEntity> TECH_BLOCK_ENTITY;

    // Метод регистрации, который следует вызвать при инициализации мода
    public static void registerBlockEntities() {
        TECH_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                new Identifier(Fbased.MOD_ID, "tech_block_entity"),
                BlockEntityType.Builder.create(TechBlockEntity::new, ModBlocks.T1).build(null)
        );
    }
}