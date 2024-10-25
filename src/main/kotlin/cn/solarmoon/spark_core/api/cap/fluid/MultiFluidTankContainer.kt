package cn.solarmoon.spark_core.api.cap.fluid

import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.neoforged.neoforge.common.util.INBTSerializable
import net.neoforged.neoforge.fluids.capability.templates.FluidTank

/**
 * 可以容纳多个储罐，且能一键解析
 */
data class MultiFluidTankContainer(//
    val tanks: MutableList<FluidTank>
): INBTSerializable<ListTag> {

    override fun serializeNBT(provider: HolderLookup.Provider): ListTag {
        val listTag = ListTag()
        tanks.forEach {
            val tankTag = it.writeToNBT(provider, CompoundTag())
            listTag.add(tankTag)
        }
        return listTag
    }

    override fun deserializeNBT(provider: HolderLookup.Provider, nbt: ListTag) {
        tanks.forEachIndexed { index, tank ->
            tank.readFromNBT(provider, nbt.getCompound(index))
        }
    }

}