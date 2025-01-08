package com.waterphage.worldgen.blockstates;

import com.mojang.serialization.Codec;
import com.waterphage.Fbased;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;

public interface ModPredicatesType<P extends ModPredicate> {
    BlockPredicateType<Layer> A1 = register("layer", Layer.CODEC);
    BlockPredicateType<Layer16> A2 = register("layer16", Layer16.CODEC);

    Codec<P> codec();
    private static <P extends ModPredicate> BlockPredicateType<P> register(String id, Codec<P> codec) {
        return Registry.register(Registries.BLOCK_PREDICATE_TYPE, new Identifier(Fbased.MOD_ID,id), () -> codec);
    }
    public static void registerpredicates(){
    }
}
