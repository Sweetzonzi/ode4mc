package test

import cn.solarmoon.spark_core.api.attachment.animation.AnimHelper
import cn.solarmoon.spark_core.api.blockentity.SyncedBlockEntity
import cn.solarmoon.spark_core.api.cap.fluid.FluidHandlerHelper
import cn.solarmoon.spark_core.api.cap.fluid.TileTank
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.state.BlockState

class be(pos: BlockPos, blockState: BlockState): SyncedBlockEntity(Cap.testbe.get(), pos, blockState) {

    val tank = TileTank(this, 5000)

    init {
        AnimHelper.Fluid.createFluidAnim(this)
    }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        tag.put(FluidHandlerHelper.FLUID, tank.writeToNBT(registries, CompoundTag()))
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        tank.readFromNBT(registries, tag.getCompound(FluidHandlerHelper.FLUID))
    }

}