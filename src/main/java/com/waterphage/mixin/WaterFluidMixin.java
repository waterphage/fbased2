package com.waterphage.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.registry.tag.FluidTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WaterFluid.class)
public class WaterFluidMixin {
    @Inject(method = "matchesType", at = @At("HEAD"), cancellable = true)
    private void fbased$matchesType(
            Fluid fluid,
            CallbackInfoReturnable<Boolean> cir
    ) {
        cir.setReturnValue(fluid.isIn(FluidTags.WATER));cir.cancel();
    }
}
