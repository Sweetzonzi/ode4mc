package cn.solarmoon.spark_core.api.util

import cn.solarmoon.spark_core.api.blockstate.IBedPartState
import cn.solarmoon.spark_core.api.blockstate.IBedPartState.Companion.PART
import cn.solarmoon.spark_core.api.blockstate.IHorizontalFacingState.Companion.FACING
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BedPart
import net.minecraft.world.level.block.state.properties.Property

object BlockUtil {

    /**
     * 使得设置的方块具有原位方块的方向属性
     */
    @JvmStatic
    fun replaceBlockWithDirection(originState: BlockState, state: BlockState, level: Level, pos: BlockPos): Boolean {
        if (state.values[FACING] != null && originState.values[FACING] != null) {
            level.setBlock(pos, state.setValue(FACING, originState.getValue(FACING)), 3)
            return true
        } else {
            level.setBlock(pos, state, 3)
            return false
        }
    }

    /**
     * 完美继承上一个方块的所有可以继承的属性
     */
    @JvmStatic
    fun inheritBlockWithAllState(stateToBeInherited: BlockState, stateToSet: BlockState): BlockState {
        var stateToSet = stateToSet
        val values = stateToBeInherited.values
        for ((key, value) in values) {
            if (stateToSet.values[key] != null) {
                stateToSet = stateToSet.setValue(key as Property<Comparable<Any>>, value as Comparable<Any>)
            }
        }
        return stateToSet
    }

    /**
     * 完美继承上一个方块的所有可以继承的属性并替换
     */
    @JvmStatic
    // 记得测试一下双方块换为单方块还会不会掉落本体
    fun replaceBlockWithAllState(stateToBeInherited: BlockState, stateToSet: BlockState, level: Level, pos: BlockPos) {
        var stateToSet = stateToSet
        val values = stateToBeInherited.values
        for ((key, value) in values) {
            if (stateToSet.values[key] != null) {
                stateToSet = stateToSet.setValue(key as Property<Comparable<Any>>, value as Comparable<Any>)
            }
        }
        if (stateToSet.values[PART] != null && values[PART] != null) replaceBedPartBlock(stateToBeInherited, stateToSet, level, pos)
        else if (values[PART] != null && stateToSet.values[PART] == null) {
            removeDoubleBlock(level, pos)
            level.setBlock(pos, stateToSet, 3)
        } else {
            level.setBlock(pos, stateToSet, 3)
        }
    }

    /**
     * 快速从一个双方块替换为另一个双方块<br></br>
     * 无需检测是否是双方块，如果不是的话什么也不会触发
     */
    @JvmStatic
    fun replaceBedPartBlock(originState: BlockState, stateTo: BlockState, level: Level, pos: BlockPos): Boolean {
        var stateTo = stateTo
        val part = originState.getValue(PART)
        if (stateTo.block is IBedPartState) {
            val direction: Direction = IBedPartState.getNeighbourDirection(part, originState.getValue(FACING))
            stateTo =
                stateTo.setValue(PART, part).setValue(FACING, originState.getValue(FACING))
            val stateTo2 = if (part == BedPart.FOOT) {
                stateTo.setValue(PART, BedPart.HEAD)
            } else stateTo.setValue(PART, BedPart.FOOT)
            level.setBlock(pos, stateTo, 18)
            level.setBlock(pos.relative(direction), stateTo2, 18)
            level.updateNeighborsAt(pos, stateTo.block)
            level.updateNeighborsAt(pos.relative(direction), stateTo.block)
            return true
        }
        return false
    }

    /**
     * 移除平面双方块
     */
    @JvmStatic
    fun removeDoubleBlock(level: Level, pos: BlockPos): Boolean {
        val state = level.getBlockState(pos)
        val block = state.block
        if (block is IBedPartState) {
            val direction: Direction = IBedPartState.getNeighbourDirection(state.getValue(PART), state.getValue(FACING))
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 18)
            level.setBlock(pos.relative(direction), Blocks.AIR.defaultBlockState(), 18)
            level.updateNeighborsAt(pos, Blocks.AIR)
            level.updateNeighborsAt(pos.relative(direction), Blocks.AIR)
            return true
        }
        return false
    }

    /**
     * 把方块快速拿到空手里
     */
    @JvmStatic
    fun getThis(
        player: Player,
        level: Level,
        pos: BlockPos,
        state: BlockState,
        hand: InteractionHand,
        needCrouching: Boolean,
        defaultSound: Boolean
    ): Boolean {
        val heldItem = player.getItemInHand(hand)
        if (hand == InteractionHand.MAIN_HAND && heldItem.isEmpty && (!needCrouching || player.isCrouching)) {
            val copy = state.getCloneItemStack(
                HitResultUtil.getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE),
                level,
                pos,
                player
            )
            val flag: Boolean = removeDoubleBlock(level, pos)
            if (!flag) level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState())
            if (defaultSound) {
                level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 1f, 1f)
            }
            player.setItemInHand(hand, copy)
            return true
        }
        return false
    }

}