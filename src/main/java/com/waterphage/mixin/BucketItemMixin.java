package com.waterphage.mixin;

import com.waterphage.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin {
    @Shadow @Final private Fluid fluid;

    @Redirect(
            method = "placeFluid",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;getBlockState()Lnet/minecraft/block/BlockState;"
            )
    )
    private BlockState fbased$placeCustomWater(FluidState state) {
        if (this.fluid == Fluids.WATER) {
            return ModBlocks.FB_WATER_S.getDefaultState().getBlockState();
        }
        return state.getBlockState();
    }
}