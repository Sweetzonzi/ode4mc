package cn.solarmoon.spark_core.api.block.caller

import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.HitResult
import net.neoforged.neoforge.common.util.TriState

/**
 * 可以改变block#use方法的触发条件<br/>
 * 例如可以使得蹲下+右键不强制使用物品而是仍然调用block#use方法（右键存1，蹲下+右键存一组之类）
 */
interface IBlockUseCaller {//

    fun getUseResult(state: BlockState, pos: BlockPos, level: Level, player: Player, heldItem: ItemStack, hitResult: HitResult, hand: InteractionHand): TriState

}