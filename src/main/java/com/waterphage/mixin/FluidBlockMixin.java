package com.waterphage.mixin;

import com.waterphage.block.ModBlocks;
import com.waterphage.block.models.FbFluid;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.waterphage.block.models.FbFluid.*;

@Mixin(FluidBlock.class)
public abstract class FluidBlockMixin {

    @Shadow
    protected net.minecraft.fluid.FlowableFluid fluid;

    @Inject(method = "hasRandomTicks", at = @At("HEAD"), cancellable = true)
    private void fbased$hasRandomTicks(
            BlockState state,
            CallbackInfoReturnable<Boolean> cir
    ) {
        FluidState f=state.getFluidState();
        if(f.isStill()&&f.isIn(FluidTags.WATER)){cir.setReturnValue(true);cir.cancel();}
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void fbased$randomTick(
            BlockState state,
            ServerWorld world,
            BlockPos pos,
            Random random,
            CallbackInfo ci
    ) {
        if (random.nextFloat()<0.1F&&(rchk(world,pos.north())||rchk(world,pos.south())||rchk(world,pos.west())||rchk(world,pos.east()))) {
            world.setBlockState(
                    pos,
                    ModBlocks.FB_WATER_S.getDefaultState().getBlockState(),
                    Block.NOTIFY_ALL
            );
            ci.cancel();
        }
    }
}