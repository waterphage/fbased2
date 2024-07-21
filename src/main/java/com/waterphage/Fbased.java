package com.waterphage;

import com.waterphage.block.*;
import com.waterphage.block.worldgen.ModChunk;
import com.waterphage.block.worldgen.blockstates.ModPredicatesType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.waterphage.block.ModKeys.FbBuildMode;

public class Fbased implements ModInitializer {
	public static final String MOD_ID = "fbased";
	public static final Logger LOGGER = LoggerFactory.getLogger("fbased");
	public static Integer fbKeyFlip=0;

	@Override
	public void onInitialize() {
		ModMaterials.registerModMaterials();
		ModBlocks.registerModBlocks();
		ModItems.registerModItems();
		ModTabs.registerModTabs();
		ModTags.registerModTags();
		ModKeys.registerModKeys();
		ModChunk.registerModChunk();
		ModPredicatesType.registerpredicates();




		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (FbBuildMode.wasPressed()) {
				fbKeyFlip=(fbKeyFlip+1) % 8;
				if (fbKeyFlip==0){
					client.player.sendMessage(Text.literal("Rule:face; axe:inward; stairs:0"), false);
				} else if (fbKeyFlip==1) {
					client.player.sendMessage(Text.literal("Rule:view; axe:outward; stairs:0i"), false);
				} else if (fbKeyFlip==2) {
					client.player.sendMessage(Text.literal("Rule:face; axe:outward; stairs:90"), false);
				} else if (fbKeyFlip==3) {
					client.player.sendMessage(Text.literal("Rule:view; axe:inward; stairs:90i"), false);
				} else if (fbKeyFlip==4) {
					client.player.sendMessage(Text.literal("Rule:face; axe:inward; stairs:180"), false);
				} else if (fbKeyFlip==5) {
					client.player.sendMessage(Text.literal("Rule:view; axe:outward; stairs:180i"), false);
				} else if (fbKeyFlip==6) {
					client.player.sendMessage(Text.literal("Rule:face; axe:outward; stairs:270"), false);
				} else if (fbKeyFlip==7) {
					client.player.sendMessage(Text.literal("Rule:view; axe:inward; stairs:270i"), false);
				}

			}
		});

	}
}
