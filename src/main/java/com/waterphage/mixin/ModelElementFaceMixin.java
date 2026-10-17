package com.waterphage.mixin;

import com.waterphage.meta.Points;
import com.waterphage.meta.ShaderEffectMaterial;
import net.minecraft.client.render.model.json.ModelElementFace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ModelElementFace.class)
public abstract class ModelElementFaceMixin implements Points.ShaderEffectFace {
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