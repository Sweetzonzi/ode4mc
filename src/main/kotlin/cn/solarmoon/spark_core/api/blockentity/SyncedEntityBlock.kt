package cn.solarmoon.spark_core.api.blockentity

import cn.solarmoon.spark_core.api.blockstate.IBedPartState
import cn.solarmoon.spark_core.api.cap.fluid.FluidHandlerHelper
import cn.solarmoon.spark_core.registry.common.SparkDataComponents
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BedPart
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.HitResult
import net.neoforged.neoforge.fluids.capability.templates.FluidTank

/**
 * 预设好的同步方块，将在玩家的各个交互中保留方块实体的所有信息
 *
 * 已经考虑了双方块情况
 */
abstract class SyncedEntityBlock(properties: Properties) : HandyEntityBlock(properties) {//

    override fun getCloneItemStack(state: BlockState, target: HitResult, level: LevelReader, pos: BlockPos, player: Player): ItemStack {
        val realPos = IBedPartState.getFootPos(state, pos)
        val origin = super.getCloneItemStack(state, target, level, realPos, player)
        level.getBlockEntity(realPos)?.saveToItem(origin, level.registryAccess())
        return origin
    }

    override fun setPlacedBy(level: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
        super.setPlacedBy(level, pos, state, placer, stack)
        level.getBlockEntity(pos)?.let { be ->
            be.applyComponentsFromItemStack(stack)
            stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(be.saveCustomOnly(level.registryAccess()))).copyTag().let { tag ->
                    stack.get(SparkDataComponents.SIMPLE_FLUID_CONTENT)?.let {
                        val tank = FluidTank(1000)
                        tank.fluid = it.copy()
                        tag.put(FluidHandlerHelper.FLUID, tank.writeToNBT(level.registryAccess(), CompoundTag()))
                    }
                    be.loadCustomOnly(tag, level.registryAccess())
                }
        }
    }

    override fun getDrops(state: BlockState, builder: LootParams.Builder): List<ItemStack> {
        val origin = super.getDrops(state, builder)
        val blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) ?: return origin
        val level = builder.level
        val stack = ItemStack(this)
        if (state.hasProperty(IBedPartState.PART) && state.getValue(IBedPartState.PART) == BedPart.HEAD) return origin
        blockEntity.saveToItem(stack, level.registryAccess())
        origin.add(stack)
        return origin
    }

}