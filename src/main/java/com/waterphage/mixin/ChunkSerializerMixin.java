package com.waterphage.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadableContainer;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
public abstract class ChunkSerializerMixin {

    @Shadow protected abstract Codec<ReadableContainer<RegistryEntry<Biome>>> createCodec(Registry<Biome> biomeRegistry);

    @Inject(method = "deserialize", at = @At("RETURN"))
    private static void onLoadChunk(ServerWorld world, PointOfInterestStorage poiStorage, ChunkPos chunkPos, NbtCompound nbt, CallbackInfoReturnable<ProtoChunk> cir) {
        ProtoChunk protoChunk = cir.getReturnValue();
        if (protoChunk != null && nbt.contains("CustomData", NbtElement.COMPOUND_TYPE)) {
            NbtCompound customData = nbt.getCompound("CustomData");
            if (customData.contains("fbased_height_map")) {
                for
                int[] heightMap = customData.getList("fbased_height_map",);
                System.out.println("Загружена карта высот для " + chunkPos);

                // Добавляем данные в ProtoChunk (через кастомную обёртку)
                ((ProtoChunkExtension) protoChunk).setHeightMap(heightMap);
            }
        }
    }

    @Inject(method = "serialize", at = @At("RETURN"))
    private static void onSaveChunk(ServerWorld world, Chunk chunk, CallbackInfoReturnable<NbtCompound> cir) {
        if (chunk instanceof ProtoChunk protoChunk) {
            NbtCompound nbt = cir.getReturnValue();
            NbtCompound customData = new NbtCompound();

            int[] heightMap = generateHeightMapForChunk(protoChunk);
            customData.putIntArray("fbased_height_map", heightMap);

            // Сохраняем данные в NBT
            nbt.put("CustomData", customData);
        }
    }

    private static int[] generateHeightMapForChunk(ProtoChunk chunk) {
        int[] heightMap = new int[256];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for(int y=0;y< chunk.getHeight()- chunk.getBottomY();y++){
                    heightMap[x * 16 + z+256*y] = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z);
                }

            }
        }
        return heightMap;
    }
}

