package com.waterphage.block.models;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import static com.waterphage.block.ModTags.FBBLOCKS;

public class FbCrpBlock extends FbBlock {
    public FbCrpBlock(Settings settings) {
        super(settings);
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
                .with(Properties.FACING, facing)
                ;
        world.setBlockState(pos,replace);
    }else {
        return ActionResult.FAIL;
    }
    return ActionResult.SUCCESS;
    }
}
