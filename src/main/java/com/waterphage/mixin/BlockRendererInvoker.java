package com.waterphage.mixin;

import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.quad.BakedQuadView;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer")
public interface BlockRendererInvoker {
    @Invoker("writeGeometry")
    void fbased$writeGeometry(
            BlockRenderContext ctx,
            ChunkModelBuilder builder,
            Vec3d offset,
            Material material,
            BakedQuadView quad,
            int[] colors,
            QuadLightData light
    );
}