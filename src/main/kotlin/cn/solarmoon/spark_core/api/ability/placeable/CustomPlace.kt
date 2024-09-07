package cn.solarmoon.spark_core.api.ability.placeable

import cn.solarmoon.spark_core.api.ability.AbilityComponents
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.BlockItemStateProperties
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.shapes.CollisionContext
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent

class CustomPlace {

    @SubscribeEvent
    fun place(event: UseItemOnBlockEvent) {
        val level = event.level
        val heldItem = event.itemStack
        val context = event.useOnContext
        val blockContext = BlockPlaceContext(context)
        AbilityComponents.PLACEABLE.forEach {
            if (heldItem.`is`(it.item)) {
                val result = Companion.place(blockContext, it.block)
                if (result.consumesAction()) {
                    event.cancelWithResult(result)
                }
            }
        }
    }

    companion object{
        @JvmStatic
        fun place(context: BlockPlaceContext, block: Block): ItemInteractionResult {
            if (!block.isEnabled(context.level.enabledFeatures())) {
                return ItemInteractionResult.FAIL
            } else if (!context.canPlace()) {
                return ItemInteractionResult.FAIL
            } else {
                val stateToPlace = getPlacementState(context, block) ?: return ItemInteractionResult.FAIL
                if (!placeBlock(context, stateToPlace)) {
                    return ItemInteractionResult.FAIL
                } else {
                    val blockpos = context.clickedPos
                    val level = context.level
                    val player = context.player
                    val itemstack = context.itemInHand
                    var blockstate1 = level.getBlockState(blockpos)
                    if (blockstate1.`is`(stateToPlace.block)) {
                        blockstate1 = updateBlockStateFromTag(blockpos, level, itemstack, blockstate1)
                        BlockItem.updateCustomBlockEntityTag(level, player, blockpos, itemstack)
                        updateBlockEntityComponents(level, blockpos, itemstack)
                        blockstate1.block.setPlacedBy(level, blockpos, blockstate1, player, itemstack)
                        if (player is ServerPlayer) {
                            CriteriaTriggers.PLACED_BLOCK.trigger(player, blockpos, itemstack)
                        }
                    }
                    val soundtype = blockstate1.getSoundType(level, blockpos, context.player)
                    level.playSound(player, blockpos, stateToPlace.getSoundType(level, blockpos, context.player).placeSound, SoundSource.BLOCKS, (soundtype.getVolume() + 1.0f) / 2.0f, soundtype.getPitch() * 0.8f)
                    level.gameEvent(GameEvent.BLOCK_PLACE, blockpos, GameEvent.Context.of(player, blockstate1))
                    itemstack.consume(1, player)
                    return ItemInteractionResult.sidedSuccess(level.isClientSide)
                }
            }
        }

        private fun getPlacementState(context: BlockPlaceContext, block: Block): BlockState? {
            val blockstate = block.getStateForPlacement(context)
            return if (blockstate != null && this.canPlace(context, blockstate)) blockstate else null
        }

        private fun updateBlockEntityComponents(level: Level, poa: BlockPos, stack: ItemStack) {
            val blockentity = level.getBlockEntity(poa)
            blockentity?.let {
                it.applyComponentsFromItemStack(stack)
                it.setChanged()
            }
        }

        private fun canPlace(context: BlockPlaceContext, state: BlockState): Boolean {
            val player = context.player
            val collisioncontext = if (player == null) CollisionContext.empty() else CollisionContext.of(player)
            return state.canSurvive(context.level, context.clickedPos) && context.level.isUnobstructed(state, context.clickedPos, collisioncontext)
        }

        private fun updateBlockStateFromTag(pos: BlockPos, level: Level, stack: ItemStack, state: BlockState): BlockState {
            val blockitemstateproperties = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY)
            if (blockitemstateproperties.isEmpty) {
                return state
            } else {
                val blockstate = blockitemstateproperties.apply(state)
                if (blockstate != state) {
                    level.setBlock(pos, blockstate, 2)
                }
                return blockstate
            }
        }

        private fun placeBlock(context: BlockPlaceContext, state: BlockState): Boolean {
            return context.level.setBlock(context.clickedPos, state, 11)
        }
    }


}