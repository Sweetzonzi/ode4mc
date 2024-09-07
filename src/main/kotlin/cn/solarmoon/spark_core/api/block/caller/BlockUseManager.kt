package cn.solarmoon.spark_core.api.block.caller

import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent

class BlockUseManager {

    @SubscribeEvent
    fun onPlayerUseOn(event: PlayerInteractEvent.RightClickBlock) {
        val level: Level = event.level
        val heldItem: ItemStack = event.itemStack
        val pos: BlockPos = event.pos
        val hit: BlockHitResult = event.hitVec
        val state = level.getBlockState(pos)
        val player: Player = event.entity
        val hand: InteractionHand = event.hand

        val block = state.block
        if (block is IBlockUseCaller) {
            event.useBlock = block.getUseResult(state, pos, level, player, heldItem, hit, hand)
        }
    }

}