
package com.waterphage.mixin;

import com.mojang.authlib.yggdrasil.request.AbuseReportRequest;
import com.waterphage.meta.WaterMaterial;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.LightType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.waterphage.meta.Storage;
import me.jellysquid.mods.sodium.client.model.color.ColorProvider;
import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.model.light.LightPipeline;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Shadow;

// Замени импорт на фактический пакет FluidRenderer из твоего Sodium.
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;

@Mixin(FluidRenderer.class)
public class FluidRendererMixin {
    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/terrain/material/DefaultMaterials;forFluidState(Lnet/minecraft/fluid/FluidState;)Lme/jellysquid/mods/sodium/client/render/chunk/terrain/material/Material;"
            )
    )
    private Material fbased$waterMaterial(FluidState state) {
        if (state.isIn(FluidTags.WATER)) {
            return WaterMaterial.INSTANCE;
        }

        return DefaultMaterials.forFluidState(state);
    }
}
