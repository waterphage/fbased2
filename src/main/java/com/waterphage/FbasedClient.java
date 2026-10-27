
package com.waterphage;

import com.waterphage.block.ModBlocks;
import com.waterphage.block.ModTags;
import com.waterphage.meta.Storage;
import net.fabricmc.api.ClientModInitializer;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.block.Block;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import java.io.IOException;

import static com.waterphage.meta.Storage.calculateColor;

public class FbasedClient implements ClientModInitializer {

	public static ShaderProgram WATER_SHADER;

	@Override
	public void onInitializeClient() {
		FluidRenderHandlerRegistry.INSTANCE.register(
				ModBlocks.FB_WATER_S,
				ModBlocks.FB_WATER_F,
				new SimpleFluidRenderHandler(
						new Identifier("minecraft", "block/water_still"),
						new Identifier("minecraft", "block/water_flow"),
						new Identifier("minecraft", "block/water_overlay"),
						0xFFFFFF
				) {
					@Override
					public int getFluidColor(
							BlockRenderView view,
							BlockPos pos,
							FluidState state
					) {
						int color = BiomeColors.getWaterColor(view, pos);
						return ColorABGR.pack(
								(color >> 16) & 0xFF,
								(color >> 8) & 0xFF,
								color & 0xFF,
								255
						);
					}
				}
		);
		CoreShaderRegistrationCallback.EVENT.register(context -> {
			try {
				context.register(
						new Identifier("fbased", "water"),
						VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL,
						shader -> WATER_SHADER = shader
				);
			} catch (IOException e) {
				throw new RuntimeException("Failed to load water shader", e);
			}
		});
		ColorProviderRegistry.BLOCK.register(
					(state, world, pos, tintIndex) -> {
						return switch (tintIndex) {
							case 0 -> calculateColor(world, pos, Storage.FOG_COLOR);
							case 1 -> calculateColor(world, pos, Storage.SKY_COLOR);
							case 2 -> calculateColor(world, pos, Storage.GRS_COLOR);
							case 3 -> calculateColor(world, pos, Storage.FLG_COLOR);
							case 4 -> calculateColor(world, pos, Storage.WTR_COLOR);
							case 5 -> calculateColor(world, pos, Storage.WFG_COLOR);
							default -> -1;
						};
					},
				ModBlocks.A1,ModBlocks.AI5);
	}
}
