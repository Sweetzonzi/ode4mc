package cn.solarmoon.spark_core.api.data

import net.minecraft.advancements.Criterion
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items


abstract class RecipeJsonBuilder: RecipeBuilder {

    protected val criteria: LinkedHashMap<String, Criterion<*>> = LinkedHashMap()
    protected var group: String? = null

    override fun unlockedBy(name: String, criterion: Criterion<*>): RecipeBuilder = apply { criteria[name] = criterion }

    override fun group(groupName: String?): RecipeBuilder = apply { this.group = groupName }

    /**
     * 决定了产出json的文件名
     */
    abstract override fun getResult(): Item

}