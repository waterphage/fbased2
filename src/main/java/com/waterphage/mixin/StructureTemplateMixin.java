package com.waterphage.mixin;

import com.waterphage.meta.ScalableStructure;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin implements ScalableStructure {

    @Unique
    @Override
    public void scaleStructure(float scale) {
        if (scale <= 0 || scale > 1) return;

        StructureTemplate self = (StructureTemplate) (Object) this;
        Vec3i originalSize = self.getSize();

        for (StructureTemplate.PalettedBlockInfoList list : self.blockInfoLists) {
            Map<Long,List<Integer>> check= new HashMap<>();
            List<StructureTemplate.StructureBlockInfo> total = new ArrayList<>();
            for (StructureTemplate.StructureBlockInfo info : list.getAll()) {
                Vec3i pos = info.pos();
                int x = Math.round(pos.getX() * scale);
                int y = Math.round(pos.getY() * scale);
                int z = Math.round(pos.getZ() * scale);
                total.add(new StructureTemplate.StructureBlockInfo(new BlockPos(x, y, z), info.state(), info.nbt()));
                Long key=BlockPos.asLong(x,y,z);
                List<Integer> cont=check.computeIfAbsent(key, k -> new ArrayList<>());
                cont.add(total.size()-1);check.put(key,cont);
            }
            list.getAll().clear();
            List<StructureTemplate.StructureBlockInfo> newInfos = new ArrayList<>();
            for (Long p:check.keySet()){
                List<Integer>pos=check.get(p);
                newInfos.add(total.get(pos.get((int)(Math.random() * pos.size()))));
            }
            list.getAll().addAll(newInfos);
        }

        // Масштабируем size
        self.size = new Vec3i(
                Math.max(1, Math.round(originalSize.getX() * scale)),
                Math.max(1, Math.round(originalSize.getY() * scale)),
                Math.max(1, Math.round(originalSize.getZ() * scale))
        );

        // Сущности тоже нужно смещать и масштабировать
        List<StructureTemplate.StructureEntityInfo> newEntities = new ArrayList<>();
        for (StructureTemplate.StructureEntityInfo entity : self.entities) {
            Vec3d pos = entity.pos;
            Vec3d newPos = new Vec3d(
                    (pos.x) * scale,
                    (pos.y) * scale,
                    (pos.z) * scale
            );
            BlockPos newBlockPos = new BlockPos(
                    Math.round(entity.blockPos.getX() * scale),
                    Math.round(entity.blockPos.getY() * scale),
                    Math.round(entity.blockPos.getZ() * scale)
            );
            newEntities.add(new StructureTemplate.StructureEntityInfo(newPos, newBlockPos, entity.nbt));
        }
        self.entities.clear();
        self.entities.addAll(newEntities);
    }
}
