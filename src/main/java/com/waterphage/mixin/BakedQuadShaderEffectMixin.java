package com.waterphage.mixin;

import com.waterphage.meta.Points;
import net.minecraft.client.render.model.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BakedQuad.class)
public abstract class BakedQuadShaderEffectMixin implements Points.ShaderEffectQuad {
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
}
