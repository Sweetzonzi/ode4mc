package cn.solarmoon.spark_core.api.blockstate

import cn.solarmoon.spark_core.api.util.DropUtil.addItemToInventory
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.ItemTags
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.FlintAndSteelItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty

interface ILitState {

    /**
     * 默认lit值
     */
    fun defaultLitValue(): Boolean {
        return false
    }

    companion object {

        @JvmStatic
        val LIT: BooleanProperty = BlockStateProperties.LIT

        /**
         * 默认类似原版LIT开启后的光照等级
         */
        @JvmStatic
        fun getCommonLightLevel(state: BlockState): Int {
            if (state.values[LIT] != null) {
                return if (state.getValue(LIT)) 13 else 0
            }
            return 0
        }

        /**
         * 打火石手动点燃
         * @return 成功返回true
         */
        @JvmStatic
        fun litByHand(state: BlockState, pos: BlockPos, level: Level, player: Player, hand: InteractionHand): Boolean {
            val heldItem = player.getItemInHand(hand)
            //打火石等点燃
            if (!state.getValue(LIT)) {
                if (heldItem.item is FlintAndSteelItem) {
                    level.playSound(
                        player,
                        pos,
                        SoundEvents.FLINTANDSTEEL_USE,
                        SoundSource.BLOCKS,
                        1.0f,
                        player.random.nextFloat() * 0.4f + 0.8f
                    )
                    level.setBlock(pos, state.setValue(BlockStateProperties.LIT, java.lang.Boolean.TRUE), 11)
                    heldItem.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand))
                    return true
                }
            }
            return false
        }

        @JvmStatic
        fun extinguishByHand(
            state: BlockState,
            pos: BlockPos,
            level: Level,
            player: Player,
            hand: InteractionHand
        ): Boolean {
            val heldItem = player.getItemInHand(hand)
            if (!state.getValue(LIT)) return false
            val potion = heldItem.get(DataComponents.POTION_CONTENTS)
            if (heldItem.`is`(Items.POTION) && potion != null && potion.`is`(Potions.WATER.delegate)) {
                if (!player.isCreative) {
                    heldItem.shrink(1)
                    addItemToInventory(player, ItemStack(Items.GLASS_BOTTLE))
                }
                level.setBlock(pos, state.setValue(LIT, false), 3)
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS)
                return true
            }
            if (heldItem.`is`(Items.WATER_BUCKET)) {
                if (!player.isCreative) {
                    heldItem.shrink(1)
                    addItemToInventory(player, ItemStack(Items.BUCKET))
                }
                level.setBlock(pos, state.setValue(LIT, false), 3)
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS)
                return true
            }
            if (heldItem.`is`(ItemTags.SHOVELS)) {
                heldItem.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand))
                level.setBlock(pos, state.setValue(LIT, false), 3)
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS)
                return true
            }
            return false
        }

        @JvmStatic
        fun controlLitByHand(
            state: BlockState,
            pos: BlockPos,
            level: Level,
            player: Player,
            hand: InteractionHand
        ): Boolean {
            return litByHand(state, pos, level, player, hand) || extinguishByHand(state, pos, level, player, hand)
        }

    }

}