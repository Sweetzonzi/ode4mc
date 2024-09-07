package cn.solarmoon.spark_core.api.util

import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

object ItemStackUtil {

    /**
     * 是否一致并且能提供足够的数量给lessStack
     * @return 第一个输入物是否与第一个输入物物品类型一致，并且第一个输入物的量应当大于第二个输入物
     */
    fun isSameAndSufficient(stackMore: ItemStack, stackLess: ItemStack, compareComponent: Boolean): Boolean {
        val nbtMatch = !compareComponent || ItemStack.isSameItemSameComponents(stackLess, stackMore)
        return stackLess.`is`(stackMore.item) && stackMore.count >= stackLess.count && nbtMatch
    }

    /**
     * 获取双手上的特定物品，优先判断主手。
     */
    fun <T : Item> getItemInHand(player: Player, item: T): ItemStack? {
        if (player.isHolding(item)) {
            if (player.mainHandItem.`is`(item)) {
                return player.mainHandItem
            } else if (player.offhandItem.`is`(item)) {
                return player.offhandItem
            }
        }
        return null
    }

}