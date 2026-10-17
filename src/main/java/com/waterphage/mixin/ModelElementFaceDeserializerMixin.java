package com.waterphage.mixin;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Type;

import com.waterphage.meta.Points;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.util.JsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.render.model.json.ModelElementFace$Deserializer")
public abstract class ModelElementFaceDeserializerMixin {

    @Inject(method = "deserialize", at = @At("RETURN"))
    private void fbased$readShaderEffect(
            JsonElement json,
            Type type,
            JsonDeserializationContext context,
            CallbackInfoReturnable<ModelElementFace> cir) {

        JsonObject object = json.getAsJsonObject();

        boolean enabled = JsonHelper.getBoolean(
                object, "shader_effect", false);

        ((Points.ShaderEffectFace) cir.getReturnValue())
                .fbased$setShaderEffect(enabled);
    }
}