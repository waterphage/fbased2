package com.waterphage.mixin;

import com.waterphage.meta.ScalableStructure;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin implements ScalableStructure {

    @Unique
    @Override
    public void scaleStructure(float scale) {
        if (scale <= 0 || scale > 1) return;

        StructureTemplate self = (StructureTemplate) (Object) this;
        Vec3i originalSize = self.getSize();

        for (StructureTemplate.PalettedBlockInfoList list : self.blockInfoLists) {
            List<StructureTemplate.StructureBlockInfo> newInfos = new ArrayList<>();
            for (StructureTemplate.StructureBlockInfo info : list.getAll()) {
                if(Math.random()>Math.pow(scale,2)){continue;}
                // смещаем блоки к центру, масштабируем и обратно
                Vec3i pos = info.pos();
                int x = Math.round(pos.getX() * scale);
                int y = Math.round(pos.getY() * scale);
                int z = Math.round(pos.getZ() * scale);
                newInfos.add(new StructureTemplate.StructureBlockInfo(new BlockPos(x, y, z), info.state(), info.nbt()));
            }
            list.getAll().clear();
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
