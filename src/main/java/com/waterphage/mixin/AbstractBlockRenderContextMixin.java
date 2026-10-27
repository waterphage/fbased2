
package com.waterphage.mixin;

import com.waterphage.meta.Points;
import com.waterphage.meta.ShaderEffectMaterial;
import link.infra.indium.renderer.render.AbstractBlockRenderContext;
import link.infra.indium.renderer.mesh.MutableQuadViewImpl;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(AbstractBlockRenderContext.class)
public abstract class AbstractBlockRenderContextMixin {

    @ModifyArg(
            method = "renderQuad",
            at = @At(
                    value = "INVOKE",
                    target = "Llink/infra/indium/renderer/render/AbstractBlockRenderContext;bufferQuad(Llink/infra/indium/renderer/mesh/MutableQuadViewImpl;Lme/jellysquid/mods/sodium/client/render/chunk/terrain/material/Material;)V",
                    remap = false
            ),
            index = 1,
            remap = false
    )
    private Material fbased$applyShaderEffect(
            Material material,
            @Local(ordinal = 0) MutableQuadViewImpl quad
    ) {

        if (((Points.ShaderEffectQuad) quad).fbased$hasShaderEffect()) {
            return new ShaderEffectMaterial(material);
        }

        return material;
    }
}
