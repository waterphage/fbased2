package com.waterphage.mixin;

import com.waterphage.meta.Points;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BakedQuadFactory;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(JsonUnbakedModel.class)
public abstract class JsonUnbakedModelMixin {

    @Inject(method = "createQuad", at = @At("RETURN"))
    private static void fbased$copyShaderEffect(
            ModelElement element,
            ModelElementFace face,
            Sprite sprite,
            Direction side,
            ModelBakeSettings settings,
            Identifier id,
            CallbackInfoReturnable<BakedQuad> cir) {

        boolean enabled =
                ((Points.ShaderEffectFace) face).fbased$hasShaderEffect();

        ((Points.ShaderEffectQuad) cir.getReturnValue())
                .fbased$setShaderEffect(enabled);
    }
}
