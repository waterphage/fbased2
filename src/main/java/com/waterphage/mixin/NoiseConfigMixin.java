package com.waterphage.mixin;

import com.waterphage.meta.FBGeoMap;
import com.waterphage.meta.IntPair;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;

@Mixin(NoiseConfig.class)
public class NoiseConfigMixin implements FBGeoMap {
    @Unique
    private List<Identifier> FBGeoMap = new ArrayList<>();
    @Unique
    @Override
    public List<Identifier> get() {
        return FBGeoMap;
    }
    @Unique
    @Override
    public void set(List<Identifier> map) {
        FBGeoMap=map;
    }
}
