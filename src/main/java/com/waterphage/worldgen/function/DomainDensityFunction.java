package com.waterphage.worldgen.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.waterphage.worldgen.placers.GeoPlacer;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.function.ToFloatFunction;
import net.minecraft.util.math.noise.InterpolatedNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.densityfunction.DensityFunctions;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public class DomainDensityFunction {

    public static Codec<? extends DensityFunction> register(Registry<Codec<? extends DensityFunction>> registry, String id, CodecHolder<? extends DensityFunction> codecHolder) {
        return (Codec)Registry.register(registry, id, codecHolder.codec());
    }

    public static Codec<? extends DensityFunction> registerAndGetDefault(Registry<Codec<? extends DensityFunction>> registry) {
        return register(registry, "spline", DensityFunctionTypes.Spline.CODEC_HOLDER);
    }

    public static DensityFunction spline(net.minecraft.util.math.Spline<DensityFunctionTypes.Spline.SplinePos, DensityFunctionTypes.Spline.DensityFunctionWrapper> spline) {
        return new DensityFunctionTypes.Spline(spline);
    }
    public static record Spline(net.minecraft.util.math.Spline<DensityFunctionTypes.Spline.SplinePos, DensityFunctionTypes.Spline.DensityFunctionWrapper> spline) implements DensityFunction {
        private static final Codec<net.minecraft.util.math.Spline<DensityFunctionTypes.Spline.SplinePos, DensityFunctionTypes.Spline.DensityFunctionWrapper>> SPLINE_CODEC;
        private static final MapCodec<DensityFunctionTypes.Spline> SPLINE_FUNCTION_CODEC;
        public static final CodecHolder<DensityFunctionTypes.Spline> CODEC_HOLDER;

        public Spline(net.minecraft.util.math.Spline<DensityFunctionTypes.Spline.SplinePos, DensityFunctionTypes.Spline.DensityFunctionWrapper> spline) {
            this.spline = spline;
        }

        public double sample(DensityFunction.NoisePos pos) {
            return (double)this.spline.apply(new DensityFunctionTypes.Spline.SplinePos(pos));
        }

        public double minValue() {
            return (double)this.spline.min();
        }

        public double maxValue() {
            return (double)this.spline.max();
        }

        public void fill(double[] densities, DensityFunction.EachApplier applier) {
            applier.fill(densities, this);
        }

        public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
            return visitor.apply(new DensityFunctionTypes.Spline(this.spline.apply((densityFunctionWrapper) -> {
                return densityFunctionWrapper.apply(visitor);
            })));
        }

        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return CODEC_HOLDER;
        }

        public net.minecraft.util.math.Spline<DensityFunctionTypes.Spline.SplinePos, DensityFunctionTypes.Spline.DensityFunctionWrapper> spline() {
            return this.spline;
        }

        static {
            SPLINE_CODEC = net.minecraft.util.math.Spline.createCodec(DensityFunctionTypes.Spline.DensityFunctionWrapper.CODEC);
            SPLINE_FUNCTION_CODEC = SPLINE_CODEC.fieldOf("spline").xmap(DensityFunctionTypes.Spline::new, DensityFunctionTypes.Spline::spline);
            CODEC_HOLDER = DensityFunctionTypes.holderOf(SPLINE_FUNCTION_CODEC);
        }

        public static record SplinePos(DensityFunction.NoisePos context) {
            public SplinePos(DensityFunction.NoisePos context) {
                this.context = context;
            }

            public DensityFunction.NoisePos context() {
                return this.context;
            }
        }

        public static record DensityFunctionWrapper(RegistryEntry<DensityFunction> function) implements ToFloatFunction<DensityFunctionTypes.Spline.SplinePos> {
            public static final Codec<DensityFunctionTypes.Spline.DensityFunctionWrapper> CODEC;

            public DensityFunctionWrapper(RegistryEntry<DensityFunction> function) {
                this.function = function;
            }

            public String toString() {
                Optional<RegistryKey<DensityFunction>> optional = this.function.getKey();
                if (optional.isPresent()) {
                    RegistryKey<DensityFunction> registryKey = (RegistryKey)optional.get();
                    if (registryKey == DensityFunctions.CONTINENTS_OVERWORLD) {
                        return "continents";
                    }

                    if (registryKey == DensityFunctions.EROSION_OVERWORLD) {
                        return "erosion";
                    }

                    if (registryKey == DensityFunctions.RIDGES_OVERWORLD) {
                        return "weirdness";
                    }

                    if (registryKey == DensityFunctions.RIDGES_FOLDED_OVERWORLD) {
                        return "ridges";
                    }
                }

                return "Coordinate[" + this.function + "]";
            }

            public float apply(DensityFunctionTypes.Spline.SplinePos splinePos) {
                return (float)((DensityFunction)this.function.value()).sample(splinePos.context());
            }

            public float min() {
                return this.function.hasKeyAndValue() ? (float)((DensityFunction)this.function.value()).minValue() : Float.NEGATIVE_INFINITY;
            }

            public float max() {
                return this.function.hasKeyAndValue() ? (float)((DensityFunction)this.function.value()).maxValue() : Float.POSITIVE_INFINITY;
            }

            public DensityFunctionTypes.Spline.DensityFunctionWrapper apply(DensityFunction.DensityFunctionVisitor visitor) {
                return new DensityFunctionTypes.Spline.DensityFunctionWrapper(new RegistryEntry.Direct(((DensityFunction)this.function.value()).apply(visitor)));
            }

            public RegistryEntry<DensityFunction> function() {
                return this.function;
            }

            static {
                CODEC = DensityFunction.REGISTRY_ENTRY_CODEC.xmap(DensityFunctionTypes.Spline.DensityFunctionWrapper::new, DensityFunctionTypes.Spline.DensityFunctionWrapper::function);
            }
        }
    }
}

