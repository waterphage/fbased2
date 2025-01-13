package com.waterphage.worldgen.rules;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registry;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class Adder {
    public record AdderMaterialRule(List<MaterialRules.MaterialRule> sequence) implements MaterialRules.MaterialRule
    {
        public static final CodecHolder<AdderMaterialRule> CODEC = CodecHolder.of(MaterialRules.MaterialRule.CODEC.listOf().xmap(AdderMaterialRule::new, AdderMaterialRule::sequence).fieldOf("sequence"));

        @Override
        public CodecHolder<? extends MaterialRules.MaterialRule> codec() {
            return CODEC;
        }

        @Override
        public MaterialRules.BlockStateRule apply(MaterialRules.MaterialRuleContext materialRuleContext) {
            if (this.sequence.size() == 1) {
                return (MaterialRules.BlockStateRule)this.sequence.get(0).apply(materialRuleContext);
            }
            ImmutableList.Builder builder = ImmutableList.builder();
            for (MaterialRules.MaterialRule materialRule : this.sequence) {
                builder.add((MaterialRules.BlockStateRule)materialRule.apply(materialRuleContext));
            }
            return new AdderBlockStateRule((List<MaterialRules.BlockStateRule>)((Object)builder.build()));
        }
    }
    private static record AdderBlockStateRule(List<MaterialRules.BlockStateRule> rules) implements MaterialRules.BlockStateRule {
        AdderBlockStateRule(List<MaterialRules.BlockStateRule> rules) {
            this.rules = rules;
        }

        @Nullable
        public BlockState tryApply(int i, int j, int k) {
            Iterator var4 = this.rules.iterator();

            BlockState blockState;
            do {
                MaterialRules.BlockStateRule blockStateRule = (MaterialRules.BlockStateRule)var4.next();
                blockState = blockStateRule.tryApply(i, j, k);
            } while(var4.hasNext());

            return blockState;
        }

        public List<MaterialRules.BlockStateRule> rules() {
            return this.rules;
        }
    }
}
