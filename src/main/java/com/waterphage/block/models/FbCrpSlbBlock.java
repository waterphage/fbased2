package com.waterphage.block.models;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import static com.waterphage.block.ModTags.FBSLABS;

public class FbCrpSlbBlock extends FbSlbBlock {
    public FbCrpSlbBlock(Settings settings, SoundEvent blockSoundEvent) {
        super(settings,blockSoundEvent);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand,
                              BlockHitResult hit) {

        if (ItemStack.areItemsEqual(new ItemStack(this), player.getStackInHand(hand)) && state.get(SINGLE)){
            ActionResult ACTION;
            ACTION = combineSlab(state, world, pos, player, hand, hit);
            return ACTION;
        }
        ItemStack itemh= new ItemStack(player.getInventory().getMainHandStack().getItem());
    if (itemh.isIn(FBSLABS)&&player.getAbilities().allowModifyWorld){
        Integer inv;
        if(!player.isCreative()){
            inv=1;
        }else {
            inv=0;
        }
        Boolean single = state.get(SINGLE);
        if(!single){
            if(player.getStackInHand(hand).getCount()>=inv*2){
                player.getStackInHand(hand).setCount(player.getStackInHand(hand).getCount()-inv*2);
            }else{
                player.sendMessage(Text.literal("Two slabs needed to proceed!"), false);
                return ActionResult.FAIL;
            }
        }else{
            player.getStackInHand(hand).setCount(player.getStackInHand(hand).getCount()-inv*1);
        }
        String itemid= "fbased:"+String.valueOf(itemh.getItem());
        Direction facing = state.get(FACING);
        Boolean water = state.get(SlabBlock.WATERLOGGED);
        world.breakBlock(pos,true);
        BlockState replace = Registries.BLOCK.get(new Identifier(itemid)).getDefaultState()
                .with(Properties.FACING, facing)
                .with(SINGLE, single)
                .with(Properties.WATERLOGGED, water)
                ;
        world.setBlockState(pos,replace);
    }else {
        return ActionResult.FAIL;
    }
    return ActionResult.SUCCESS;
    }
}
