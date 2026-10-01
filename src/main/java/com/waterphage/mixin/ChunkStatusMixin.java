package com.waterphage.mixin;

import com.mojang.datafixers.util.Either;
import com.waterphage.meta.ChunkExtension;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;

@Mixin(ChunkStatus.class)
public abstract class ChunkStatusMixin {
    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/ChunkStatus;register(Ljava/lang/String;Lnet/minecraft/world/chunk/ChunkStatus;ILjava/util/EnumSet;Lnet/minecraft/world/chunk/ChunkStatus$ChunkType;Lnet/minecraft/world/chunk/ChunkStatus$SimpleGenerationTask;)Lnet/minecraft/world/chunk/ChunkStatus;"
            )
    )
    private static ChunkStatus redirectFeatureRegistration(
            String id,
            ChunkStatus previous,
            int taskMargin,
            EnumSet<Heightmap.Type> heightmaps,
            ChunkStatus.ChunkType chunkType,
            ChunkStatus.SimpleGenerationTask task
    ) {
        if (!id.equals("features")) {
            return ChunkStatus.register(
                    id,
                    previous,
                    taskMargin,
                    heightmaps,
                    chunkType,
                    task
            );
        }

        return ChunkStatus.register(
                id,
                previous,
                taskMargin,
                heightmaps,
                chunkType,
                (targetStatus, world, generator, chunks, chunk) -> {
                    Heightmap.populateHeightmaps(
                            chunk,
                            EnumSet.of(
                                    Heightmap.Type.MOTION_BLOCKING,
                                    Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                                    Heightmap.Type.OCEAN_FLOOR,
                                    Heightmap.Type.WORLD_SURFACE
                            )
                    );
                    ChunkRegion region = new ChunkRegion(world, chunks, targetStatus, 1);
                    Registry<PlacedFeature> registry = world.getRegistryManager() .get(RegistryKeys.PLACED_FEATURE);
                    PlacedFeature feature = registry.get(new Identifier("fbased", "surface"));
                    if (feature != null) {
                        ChunkRandom random = new ChunkRandom( Random.create() );
                        random.setPopulationSeed( world.getSeed(), chunk.getPos().x, chunk.getPos().z );
                        ChunkPos pos = chunk.getPos();
                        BlockPos origin = new BlockPos( pos.getStartX(), 0, pos.getStartZ() );
                        feature.generate(region, generator, random, origin);
                    }
                }
        );
    }

    // Intercept the 8-argument register method during class initialization
    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/ChunkStatus;register(Ljava/lang/String;Lnet/minecraft/world/chunk/ChunkStatus;IZLjava/util/EnumSet;Lnet/minecraft/world/chunk/ChunkStatus$ChunkType;Lnet/minecraft/world/chunk/ChunkStatus$GenerationTask;Lnet/minecraft/world/chunk/ChunkStatus$LoadTask;)Lnet/minecraft/world/chunk/ChunkStatus;"
            )
    )
    private static ChunkStatus redirectInitializeLightRegistration(
            String id, ChunkStatus previous, int taskMargin, boolean shouldAlwaysUpgrade,
            EnumSet<Heightmap.Type> heightMapTypes, ChunkStatus.ChunkType chunkType,
            ChunkStatus.GenerationTask generationTask, ChunkStatus.LoadTask loadTask
    ) {
        // We only want to intervene when the game is registering "initialize_light"
        if (id.equals("initialize_light")) {

            // 1. Register OUR status first.
            // Notice we pass the 'previous' argument here (which is FEATURES from the vanilla call)
            ChunkStatus myStatus = ChunkStatus.register(
                    "surface_refinement",
                    previous,
                    8,
                    false,
                    ChunkStatus.POST_CARVER_HEIGHTMAPS,
                    ChunkStatus.ChunkType.PROTOCHUNK,
                    (targetStatus, executor, world, generator, structureTemplateManager, lightingProvider, fullChunkConverter, chunks, chunk) -> {

                        // --- YOUR 3x3 SURFACE REFINEMENT LOGIC HERE ---
                        //if (chunk instanceof ChunkExtension ext){ext.count();}
                        //System.out.println("PHASE: SURFACE_REFINEMENT running for chunk: " + chunk.getPos()); // <--- ADD THIS
                        //Heightmap.populateHeightmaps(chunk, EnumSet.of(Heightmap.Type.MOTION_BLOCKING, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, Heightmap.Type.OCEAN_FLOOR, Heightmap.Type.WORLD_SURFACE));
                        ChunkRegion chunkRegion = new ChunkRegion(world, chunks, targetStatus, 1);
                        generator.generateFeatures(chunkRegion, chunk, world.getStructureAccessor().forRegion(chunkRegion));
                        Blender.tickLeavesAndFluids(chunkRegion, chunk);

                        return CompletableFuture.completedFuture(Either.left(chunk));
                    },
                    (targetStatus, world, structureTemplateManager, lightingProvider, fullChunkConverter, chunk) ->
                            CompletableFuture.completedFuture(Either.left(chunk))
            );

            // 2. Now register "initialize_light", but pass OUR custom status as its 'previous' step!
            // This links the chain: FEATURES -> SURFACE_REFINEMENT -> INITIALIZE_LIGHT
            return ChunkStatus.register(
                    id, myStatus, taskMargin, shouldAlwaysUpgrade, heightMapTypes, chunkType, generationTask, loadTask
            );
        }
        // For all other vanilla statuses, just let them register normally
        return ChunkStatus.register(
                id, previous, taskMargin, shouldAlwaysUpgrade, heightMapTypes, chunkType, generationTask, loadTask
        );
    }
}
