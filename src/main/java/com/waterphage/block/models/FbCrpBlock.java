package com.waterphage.block.models;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import static com.waterphage.block.ModTags.FBBLOCKS;

public class FbCrpBlock extends FbBlock implements Waterloggable {
    public FbCrpBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(Properties.WATERLOGGED, false));
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.FACING);
        stateManager.add(Properties.WATERLOGGED);

    }
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
    ItemStack itemh= new ItemStack(player.getInventory().getMainHandStack().getItem());
    if (itemh.isIn(FBBLOCKS)){
        if(!player.isCreative()){
            player.getStackInHand(hand).setCount(player.getStackInHand(hand).getCount()-1);
        }
        String itemid= "fbased:"+String.valueOf(itemh.getItem());
        Direction facing = state.get(FACING);
        world.breakBlock(pos,true);
        BlockState replace = Registries.BLOCK.get(new Identifier(itemid)).getDefaultState()
                .with(Properties.FACING, facing);
        world.setBlockState(pos,replace);
    }else {
        return ActionResult.FAIL;
    }
    return ActionResult.SUCCESS;
    }

    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1.0F;
    }

    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }
    @Override
    public FluidState getFluidState(BlockState state) {
        if (state.get(Properties.WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return state.getFluidState();

    }
}
