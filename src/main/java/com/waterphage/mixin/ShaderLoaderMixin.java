
package com.waterphage.mixin;

import me.jellysquid.mods.sodium.client.gl.shader.ShaderLoader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Mixin(ShaderLoader.class)
public class ShaderLoaderMixin {

    @Inject(method = "getShaderSource", at = @At("HEAD"), cancellable = true)
    private static void fbased$overrideShader(
            Identifier name,
            CallbackInfoReturnable<String> cir) {

        if (!name.getNamespace().equals("sodium")) {
            return;
        }

        String path = null;

        if (name.getPath().equals("blocks/block_layer_opaque.vsh")) {
            path = "/assets/fbased/shaders/sodium/block_layer_opaque.vsh";
        } else if (name.getPath().equals("blocks/block_layer_opaque.fsh")) {
            path = "/assets/fbased/shaders/sodium/block_layer_opaque.fsh";
        }else if (name.getPath().equals("include/chunk_vertex.glsl")) {
            path = "/assets/fbased/shaders/sodium/chunk_vertex.glsl";
        }


        if (path == null) {
            return;
        }

        try (InputStream in = ShaderLoaderMixin.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new RuntimeException("Shader not found in fbased resources: " + path);
            }

            cir.setReturnValue(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            System.out.println("[fbased] Loaded custom Sodium shader: " + path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load custom Sodium shader: " + path, e);
        }
    }
}
