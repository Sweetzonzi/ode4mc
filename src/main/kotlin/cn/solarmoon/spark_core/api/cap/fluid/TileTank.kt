package cn.solarmoon.spark_core.api.cap.fluid

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.attachment.animation.AnimHelper
import cn.solarmoon.spark_core.registry.common.SparkAttachments
import cn.solarmoon.spark_core.registry.common.SparkDataComponents
import net.minecraft.core.component.DataComponentMap
import net.minecraft.world.level.block.entity.BlockEntity
import net.neoforged.neoforge.fluids.SimpleFluidContent
import net.neoforged.neoforge.fluids.capability.templates.FluidTank
import test.be

/**
 * 会当液体变化时自动进行方块实体的同步
 */
open class TileTank(open val blockEntity: BlockEntity, capacity: Int): FluidTank(capacity) {

    override fun onContentsChanged() {
        super.onContentsChanged()
        // 保存液体数据方便存到item内
        blockEntity.setComponents(DataComponentMap.builder()
            .addAll(blockEntity.components())
            .set(SparkDataComponents.SIMPLE_FLUID_CONTENT, SimpleFluidContent.copyOf(fluid)).build())
        AnimHelper.Fluid.startFluidAnim(blockEntity, fluid)
        blockEntity.invalidateCapabilities()
        blockEntity.setChanged()
    }

}