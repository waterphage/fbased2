package com.waterphage.block.models;

import com.ibm.icu.impl.PropsVectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.Direction;

import static com.waterphage.Fbased.fbKeyFlip;

public class FbBlock extends FacingBlock  {

    public FbBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(Properties.FACING, Direction.UP));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(
                new Property[]{FACING}
        );
    }
    @Environment(EnvType.CLIENT)
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        if (fbKeyFlip==0||fbKeyFlip==4) {
            return this.getDefaultState().with(Properties.FACING, ctx.getSide().getOpposite());
        } else if (fbKeyFlip==1||fbKeyFlip==5) {
            return this.getDefaultState().with(Properties.FACING, ctx.getPlayerLookDirection().getOpposite());
        } else if (fbKeyFlip==2||fbKeyFlip==6) {
            return this.getDefaultState().with(Properties.FACING, ctx.getSide());
        } else {
            return this.getDefaultState().with(Properties.FACING, ctx.getPlayerLookDirection());
        }
    }
}
