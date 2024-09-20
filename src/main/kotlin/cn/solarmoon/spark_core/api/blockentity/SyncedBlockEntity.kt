package cn.solarmoon.spark_core.api.blockentity

import cn.solarmoon.spark_core.SparkCore.LOGGER
import cn.solarmoon.spark_core.api.cap.fluid.FluidHandlerHelper
import cn.solarmoon.spark_core.registry.common.SparkDataComponents
import com.mojang.logging.LogUtils
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponentMap
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.SimpleFluidContent
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack
import net.neoforged.neoforge.fluids.capability.templates.FluidTank

/**
 * setChanged调用时同步所有可保存的数据
 */
abstract class SyncedBlockEntity(type: BlockEntityType<*>, pos: BlockPos, blockState: BlockState) : BlockEntity(type, pos, blockState) {

    override fun getUpdateTag(registries: HolderLookup.Provider): CompoundTag {
        return super.saveWithoutMetadata(registries)
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun setChanged() {
        super.setChanged()
        level?.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_CLIENTS)
    }

    override fun saveToItem(stack: ItemStack, registries: HolderLookup.Provider) {
        super.saveToItem(stack, registries)
        if (stack.has(SparkDataComponents.SIMPLE_FLUID_CONTENT)) {
            val tag = saveCustomOnly(registries)
            if (tag.contains(FluidHandlerHelper.FLUID)) {
                stack.set(SparkDataComponents.SIMPLE_FLUID_CONTENT, SimpleFluidContent.copyOf(FluidTank(1000).readFromNBT(registries, tag.getCompound(FluidHandlerHelper.FLUID)).fluid))
            }
        }
    }

}