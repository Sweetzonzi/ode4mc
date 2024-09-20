package cn.solarmoon.spark_core.api.data

import net.minecraft.advancements.AdvancementHolder
import net.minecraft.advancements.Criterion
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Recipe


abstract class RecipeJsonBuilder: RecipeBuilder {

    protected val criteria: LinkedHashMap<String, Criterion<*>> = LinkedHashMap()
    protected var group: String? = null

    override fun unlockedBy(name: String, criterion: Criterion<*>): RecipeBuilder = apply { criteria[name] = criterion }

    override fun group(groupName: String?): RecipeBuilder = apply { this.group = groupName }

    override fun getResult(): Item = Items.AIR

    /**
     * 生成的文件名（res的前半部分决定了data.XXX的名字（也就是模组名），后半部分决定了生成的文件本身的名字）
     */
    abstract val name: ResourceLocation

    /**
     * 生成的文件路径前缀（比如空则为/data/name，填入以后则为/data/custom/name）
     */
    abstract val prefix: String

    override fun save(recipeOutput: RecipeOutput) {
        save(recipeOutput, name)
    }

    override fun save(recipeOutput: RecipeOutput, id: ResourceLocation) {
        val loc = prefix.takeIf { !it.isEmpty() }?.let { id.withPrefix("$prefix/") } ?: id
        recipeOutput.accept(loc, getRecipe(recipeOutput, loc), getAdvancement(recipeOutput, loc))
    }

    abstract fun getRecipe(recipeOutput: RecipeOutput, location: ResourceLocation): Recipe<*>

    open fun getAdvancement(recipeOutput: RecipeOutput, location: ResourceLocation): AdvancementHolder? = null

}