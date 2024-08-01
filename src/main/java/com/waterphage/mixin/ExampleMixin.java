package com.waterphage.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.noise.NoiseRouter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
public class ExampleMixin {
	@Mixin(ChunkNoiseSampler.class)
	public interface createMultiNoiseSamplerInvoker {
		@Invoker("createMultiNoiseSampler")
		MultiNoiseUtil.MultiNoiseSampler createMultiNoiseSampler(NoiseRouter noiseRouter, List<MultiNoiseUtil.NoiseHypercube> spawnTarget);
	}
	@Mixin(ChunkNoiseSampler.class)
	public interface sampleBlockStateInvoker {
		@Invoker("sampleBlockState")
		BlockState sampleBlockState();
	}

	@Mixin(ChunkNoiseSampler.class)
	public interface getHorizontalCellBlockCountInvoker {
		@Invoker("getHorizontalCellBlockCount")
		int getHorizontalCellBlockCount();
	}

	@Mixin(ChunkNoiseSampler.class)
	public interface getVerticalCellBlockCountInvoker {
		@Invoker("getVerticalCellBlockCount")
		int getVerticalCellBlockCount();
	}
}