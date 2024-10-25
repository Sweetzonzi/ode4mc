package cn.solarmoon.spark_core.feature.inlay

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.cap.item.ItemStackHandlerHelper
import cn.solarmoon.spark_core.registry.common.SparkRecipes
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.AnvilUpdateEvent


class AnvilInlayModifier {//

    @SubscribeEvent
    fun onAnvilUpdate(event: AnvilUpdateEvent) {
        val left = event.left
        val right = event.right
        val level = event.player.level()

        //这里有三个条件：1.输入物匹配 2.镶嵌物匹配且数量足够 3.输入物已镶嵌的该镶嵌物数量小于最大镶嵌数
        fun findCheckedRecipe(): AttributeForgingRecipe? {
            val recipes = level.recipeManager.getAllRecipesFor(SparkRecipes.ATTRIBUTE_FORGING.type.get())
            return recipes.map { it.value }.firstOrNull {
                it.input.test(left)
                        && it.isMaterialSufficient(right)
                        && InlayHelper.getSameItemCount(it.material.item, left) < it.maxForgeCount
            }
        }

        SparkCore.LOGGER.info(findCheckedRecipe()?.let { toString() } ?: "null")

        findCheckedRecipe()?.let {
            val result = left.copyWithCount(1)
            val handler = InlayHelper.getInlayHandler(result)
            ItemStackHandlerHelper.insertItem(handler, it.material) // 插入材料
            InlayHelper.addAttributeToItem(result, it) // 给属性
            event.output = result
            event.cost = it.expCost // 经验消耗
            event.materialCost = it.material.count // 材料数量消耗
        }

    }

}