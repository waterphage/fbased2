package com.waterphage.block.models;

import com.ibm.icu.impl.PropsVectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
        setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.UP));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction dir;
        switch (fbKeyFlip) {
            case 0, 4 -> dir = ctx.getSide().getOpposite();
            case 1, 5 -> dir = ctx.getPlayerLookDirection().getOpposite();
            case 2, 6 -> dir = ctx.getSide();
            default   -> dir = ctx.getPlayerLookDirection();
        }
        return getDefaultState().with(FACING, dir);
    }
}
