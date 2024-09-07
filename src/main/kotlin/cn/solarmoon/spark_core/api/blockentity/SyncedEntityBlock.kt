package cn.solarmoon.spark_core.api.blockentity

import cn.solarmoon.spark_core.api.blockstate.IBedPartState
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BedPart
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.HitResult

/**
 * 预设好的同步方块，将在玩家的各个交互中保留方块实体的所有信息
 */
abstract class SyncedEntityBlock(properties: Properties) : HandyEntityBlock(properties) {

    override fun getCloneItemStack(state: BlockState, target: HitResult, level: LevelReader, pos: BlockPos, player: Player): ItemStack {
        val origin = super.getCloneItemStack(state, target, level, pos, player)
        level.getBlockEntity(pos)?.saveToItem(origin, level.registryAccess())
        return origin
    }

    override fun setPlacedBy(level: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
        super.setPlacedBy(level, pos, state, placer, stack)
        stack.get(DataComponents.BLOCK_ENTITY_DATA)?.copyTag()?.let {
            level.getBlockEntity(pos)?.loadCustomOnly(it, level.registryAccess())
        }
    }

    override fun getDrops(state: BlockState, builder: LootParams.Builder): List<ItemStack> {
        val blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY)
        val drops = super.getDrops(state, builder)
        val t = ItemStack(this)
        blockEntity?.let {
            it.level?.let { level ->
                it.saveToItem(t, level.registryAccess())
                // 防止双方块多次掉落
                if (this is IBedPartState) {
                    if (state.getValue(IBedPartState.PART) === BedPart.HEAD) drops.add(t)
                    else return drops
                } else drops.add(t)
            }
        }
        return drops
    }

}