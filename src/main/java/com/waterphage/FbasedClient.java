package com.waterphage;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class FbasedClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {

		//BlockRenderLayerMap.INSTANCE.putBlock(Registries.BLOCK.get(new Identifier(Fbased.MOD_ID,name+"_raw")), RenderLayer.getCutout());
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
	}
}