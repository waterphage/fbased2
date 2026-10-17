
package com.waterphage;

import com.waterphage.block.ModBlocks;
import com.waterphage.block.ModTags;
import com.waterphage.meta.Storage;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

import java.io.IOException;

import static com.waterphage.meta.Storage.calculateColor;

public class FbasedClient implements ClientModInitializer {

	public static ShaderProgram WATER_SHADER;

	@Override
	public void onInitializeClient() {
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
						if (world == null || pos == null) return -1;
						if (!state.isIn(ModTags.FBTINT)) return -1;

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
					ModBlocks.AI5
			);
	}
}
