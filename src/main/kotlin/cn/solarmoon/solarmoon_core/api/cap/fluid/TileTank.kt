package cn.solarmoon.solarmoon_core.api.cap.fluid

import cn.solarmoon.solarmoon_core.api.attachment.animation.AnimHelper
import cn.solarmoon.solarmoon_core.registry.common.CommonAttachments
import net.minecraft.world.level.block.entity.BlockEntity
import net.neoforged.neoforge.fluids.capability.templates.FluidTank

/**
 * 会当液体变化时自动进行方块实体的同步
 */
class TileTank(val blockEntity: BlockEntity, capacity: Int): FluidTank(capacity) {

    override fun onContentsChanged() {
        super.onContentsChanged()
        // 你肯定很好奇只是启动动画为什么不需要setchanged了，我也很好奇，甚至调用setchanged会导致动画不平滑，双端问题是这样的
        if (blockEntity.hasData(CommonAttachments.ANIMTICKER)) {
            AnimHelper.Fluid.startFluidAnim(blockEntity, fluid)
        } else {
            blockEntity.invalidateCapabilities()
            blockEntity.setChanged()
        }
    }

}