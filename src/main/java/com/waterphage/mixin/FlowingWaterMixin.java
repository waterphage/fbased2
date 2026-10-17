package com.waterphage.mixin;

import com.waterphage.meta.Storage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.waterphage.meta.Storage.*;
import static com.waterphage.meta.Storage.MIST;

@Mixin(FlowableFluid.class)
public abstract class FlowingWaterMixin {
    @Inject(method = "tryFlow", at = @At("HEAD"), cancellable = true)
    private void fbased$tryFlow(
            World world,
            BlockPos fluidPos,
            FluidState state,
            CallbackInfo ci
    ) {
        if (world.getBlockState(fluidPos.down()).isOf(Registries.BLOCK.get(new Identifier("fbased:mist")))){ci.cancel();}
    }
    @Inject(
            method = "onScheduledTick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void fbased$replaceDecreasingWater(
            World world,
            BlockPos pos,
            FluidState state,
            CallbackInfo ci
    ) {
        if (!state.isStill()) {
            FluidState fluidState =  ((FlowableFluid)(Object)this).getUpdatedState(world, pos, world.getBlockState(pos));
            int i =  ((FlowableFluid)(Object)this).getNextTickDelay(world, pos, state, fluidState);
            if (fluidState.isEmpty()) {
                if (world.getFluidState(pos.down(2)).isIn(FluidTags.WATER)) {
                    world.setBlockState(pos, MIST, Block.NOTIFY_ALL);
                    ci.cancel();
                    return;
                }
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
            } else if (!fluidState.equals(state)) {
                state = fluidState;
                BlockState blockState = fluidState.getBlockState();
                world.setBlockState(pos, blockState, 2);
                world.scheduleFluidTick(pos, fluidState.getFluid(), i);
                world.updateNeighborsAlways(pos, blockState.getBlock());
            }
        }

        ((FlowableFluid)(Object)this).tryFlow(world, pos, state);
    }
    @Inject(method = "flow", at = @At("TAIL"))
    private void fbased$flowDown(
            WorldAccess world,
            BlockPos pos,
            BlockState state,
            Direction direction,
            FluidState fluidState,
            CallbackInfo ci
    ) {
        if (direction != Direction.DOWN) return;
        if (!fluidState.isIn(FluidTags.WATER)) return;
        if (world.isClient()) return;


        if (world.getRandom().nextFloat() > 0.9) return;
        BlockPos p=pos.up(15);
        if(!world.getFluidState(p).isOf(FLOW)&&!world.getBlockState(p).isOf(MIST_B)){return;}

        p=pos.down();
        BlockState st=world.getBlockState(p);
        if (st.isAir()){
            world.setBlockState(p, MIST, Block.NOTIFY_ALL);
        } else if (st.getFluidState().isOf(FLOW)) {
            world.setBlockState(p, MIST, Block.NOTIFY_ALL);
        }else {return;}
        p=pos;
        world.setBlockState(p, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
        p=pos.up();
        if(world.getFluidState(p).isOf(FLOW)){
            world.setBlockState(p, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
        }

    }
}