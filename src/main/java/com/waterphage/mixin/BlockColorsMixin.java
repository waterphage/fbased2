package com.waterphage.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.ColorResolver;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockColors.class)
public class BlockColorsMixin {
    private static final ColorResolver FOG_COLOR =
            (biome, x, z) -> biome.getFogColor();

    private static final ColorResolver SKY_COLOR =
            (biome, x, z) -> biome.getSkyColor();

    @Inject(
            method = "getColor",
            at = @At("HEAD"),
            cancellable = true
    )
    private void fbased$getSpecialColor(
            BlockState state,
            @Nullable BlockRenderView world,
            @Nullable BlockPos pos,
            int tintIndex,
            CallbackInfoReturnable<Integer> cir
    ) {
        if (world == null || pos == null) {
            return;
        }

        if (tintIndex == 100) {
            cir.setReturnValue(world.getColor(pos, FOG_COLOR));
        }
        else if (tintIndex == 101) {
            cir.setReturnValue(world.getColor(pos, SKY_COLOR));
        }
    }
}