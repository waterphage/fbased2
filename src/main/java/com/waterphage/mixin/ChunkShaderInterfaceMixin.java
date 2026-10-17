package com.waterphage.mixin;

import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformFloat;
import net.minecraft.client.MinecraftClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkShaderInterface.class)
public abstract class ChunkShaderInterfaceMixin {

    @Unique
    private GlUniformFloat fbased$skyBrightness;

    @Inject(
            method = "<init>",
            at = @At("RETURN"),
            remap = false
    )
    private void fbased$init(
            ShaderBindingContext context,
            ChunkShaderOptions options,
            CallbackInfo ci
    ) {
        this.fbased$skyBrightness =
                context.bindUniform("u_SkyBrightness", GlUniformFloat::new);
    }

    @Inject(
            method = "setupState",
            at = @At("HEAD"),
            remap = false
    )
    private void fbased$updateSkyBrightness(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        float brightness = client.world != null
                ? client.world.getSkyBrightness(1.0F)
                : 1.0F;

        this.fbased$skyBrightness.set(brightness);
    }
}