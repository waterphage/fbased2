package com.waterphage.mixin;

import com.waterphage.meta.Points;
import link.infra.indium.renderer.mesh.MutableQuadViewImpl;
import net.minecraft.client.render.model.BakedQuad;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MutableQuadViewImpl.class)
public abstract class MutableQuadViewMixin implements Points.ShaderEffectQuad {

    @Unique
    private boolean fbased$shaderEffect;

    @Override
    public boolean fbased$hasShaderEffect() {
        return fbased$shaderEffect;
    }

    @Override
    public void fbased$setShaderEffect(boolean value) {
        fbased$shaderEffect = value;
    }

    @Inject(method = "clear", at = @At("HEAD"), remap = false)
    private void fbased$clearShaderEffect(CallbackInfo ci) {
        fbased$shaderEffect = false;
    }

    @Inject(
            method = "fromVanilla(Lnet/minecraft/client/render/model/BakedQuad;Lnet/fabricmc/fabric/api/renderer/v1/material/RenderMaterial;Lnet/minecraft/util/math/Direction;)Llink/infra/indium/renderer/mesh/MutableQuadViewImpl;",
            at = @At("RETURN"),
            remap = true
    )
    private void fbased$copyShaderEffect(
            BakedQuad quad,
            RenderMaterial material,
            Direction cullFace,
            CallbackInfoReturnable<MutableQuadViewImpl> cir
    ) {
        if (quad instanceof Points.ShaderEffectQuad source) {
            this.fbased$setShaderEffect(source.fbased$hasShaderEffect());
        }
    }
}