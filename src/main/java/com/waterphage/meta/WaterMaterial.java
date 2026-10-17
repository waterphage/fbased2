
package com.waterphage.meta;

import me.jellysquid.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.parameters.AlphaCutoffParameter;

public class WaterMaterial extends Material {
    public static final int WATER_FLAG = 1 << 3;

    public static final Material INSTANCE = new WaterMaterial();

    private WaterMaterial() {
        super(
                DefaultTerrainRenderPasses.TRANSLUCENT,
                AlphaCutoffParameter.ZERO,
                true
        );
    }

    @Override
    public int bits() {
        return super.bits() | WATER_FLAG;
    }
}
