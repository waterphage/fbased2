package com.waterphage.block.models;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FbBurning extends FbBlock{
    private boolean putoff=false;
    private float speed=1.0F;
    private BlockState ash;

    public FbBurning(Identifier ash,boolean putoff,float speed,Settings settings) {
        super(settings);
        this.putoff=putoff;
        this.speed=speed;
        this.ash=Registries.BLOCK.get(ash).getDefaultState();
    }
    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }
    @Override
    public void neighborUpdate(
            BlockState state,
            World world,
            BlockPos pos,
            Block sourceBlock,
            BlockPos sourcePos,
            boolean notify
    ) {
        if (!world.isClient&&putoff) {
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = pos.offset(direction);
                if (world.getFluidState(neighborPos).isOf(Fluids.WATER)) {
                    world.setBlockState(pos, ash.with(Properties.FACING,state.get(Properties.FACING)));return;
                }
            }
        }
    }
    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        for (Direction direction : Direction.values()) {
            BlockPos firePos = pos.offset(direction);
            if (world.getBlockState(firePos).isAir()) {world.setBlockState(firePos, Blocks.FIRE.getDefaultState());
            }
        }
    }
    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
       if (random.nextFloat()>speed){
           for (Direction direction : Direction.values()) {
               BlockPos firePos = pos.offset(direction);
               if (world.getBlockState(firePos).isAir()) {world.setBlockState(firePos, Blocks.FIRE.getDefaultState());
               }
           }
       }else{
           world.setBlockState(pos, ash.with(Properties.FACING,state.get(Properties.FACING)));
       }
   }
}
