package com.waterphage.mixin;

import com.waterphage.block.ModTags;
import com.waterphage.meta.Storage;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.world.BiomeColorCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.ColorResolver;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.waterphage.meta.Storage.*;

@Mixin(BlockColors.class)
public class BlockColorsMixin {





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
        if (!state.isIn(ModTags.FBTINT)) {
            return;
        }
        if (world == null || pos == null) {
            return;
        }
        switch (tintIndex) {
            case 0: cir.setReturnValue(calculateColor(world, pos, FOG_COLOR));return;
            case 1: cir.setReturnValue(calculateColor(world, pos, SKY_COLOR));return;
            case 2: cir.setReturnValue(calculateColor(world, pos, GRS_COLOR));return;
            case 3: cir.setReturnValue(calculateColor(world, pos, FLG_COLOR));return;
            case 4: cir.setReturnValue(calculateColor(world, pos, WTR_COLOR));return;
            case 5: cir.setReturnValue(calculateColor(world, pos, WFG_COLOR));return;
            default: return;
        }
    }
}