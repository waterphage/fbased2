package com.waterphage.meta;

import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;

public final class ShaderEffectMaterial extends Material {
    private final Material original;


    public ShaderEffectMaterial(Material original) {
        super(original.pass, original.alphaCutoff, original.mipped);
        this.original = original;
    }

    @Override
    public int bits() {
        return original.bits() | (1 << 3);
    }
}