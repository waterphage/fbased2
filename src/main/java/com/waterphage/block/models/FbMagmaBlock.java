package com.waterphage.block.models;

import net.minecraft.block.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import static com.waterphage.block.ModTags.FBFLOW;

public class FbMagmaBlock extends Block {

    public FbMagmaBlock(AbstractBlock.Settings settings) {super(settings);}
    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.bypassesSteppingEffects() && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)entity)) {
            entity.damage(world.getDamageSources().hotFloor(), 1.0F);
        }

        super.onSteppedOn(world, pos, state, entity);
    }
    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        if (world.getRandom().nextFloat()<state.getLuminance()/15.0F){
            world.setBlockState(pos,Blocks.LAVA.getDefaultState(),Block.NOTIFY_ALL);
        }
    }
    @Override
    public void scheduledTick(
            BlockState state,
            ServerWorld world,
            BlockPos pos,
            Random random
    ) {
        BlockPos above = pos.up();
        FluidState water = world.getFluidState(above);
        if (water.isIn(FBFLOW)) {world.setBlockState(above, Registries.BLOCK.get(new Identifier("fbased:mist")).getDefaultState());}
    }
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        BlockPos up=pos.up();
        FluidState f=world.getFluidState(up);
        if (f.isIn(FBFLOW)) {
            world.setBlockState(up,Registries.BLOCK.get(new Identifier("fbased:mist")).getDefaultState());
            world.scheduleBlockTick(pos, this, 20);
            world.playSound(null,pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
        } else {
            world.scheduleBlockTick(pos, this, 40);
        }

    }
    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleBlockTick(pos, this, 20);
    }
}

