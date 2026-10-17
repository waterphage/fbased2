package com.waterphage.mixin;

import com.waterphage.meta.Points;
import com.waterphage.meta.ShaderEffectMaterial;
import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.quad.BakedQuadView;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockRenderer.class)
public abstract class BlockRendererMixin {
    @Redirect(
            method = "renderQuadList",
            at = @At(
                    value = "INVOKE",
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;writeGeometry(Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext;Lme/jellysquid/mods/sodium/client/render/chunk/compile/buffers/ChunkModelBuilder;Lnet/minecraft/util/math/Vec3d;Lme/jellysquid/mods/sodium/client/render/chunk/terrain/material/Material;Lme/jellysquid/mods/sodium/client/model/quad/BakedQuadView;[ILme/jellysquid/mods/sodium/client/model/light/data/QuadLightData;)V"
            )
    )
    private void fbased$applyShaderEffect(
            BlockRenderer instance,
            BlockRenderContext ctx,
            ChunkModelBuilder builder,
            Vec3d offset,
            Material material,
            BakedQuadView quad,
            int[] colors,
            QuadLightData light
    ) {
        Material selected = material;

        if (((Points.ShaderEffectQuad) quad).fbased$hasShaderEffect()) {
            selected = new ShaderEffectMaterial(material);
        }
        ((BlockRendererInvoker) instance).fbased$writeGeometry(
                ctx, builder, offset, selected, quad, colors, light
        );
    }
}