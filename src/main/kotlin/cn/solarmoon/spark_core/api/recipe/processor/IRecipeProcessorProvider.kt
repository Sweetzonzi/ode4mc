package cn.solarmoon.spark_core.api.recipe.processor

import net.minecraft.world.item.crafting.RecipeType

interface IRecipeProcessorProvider {

    fun getRecipeProcessors(): MutableMap<RecipeType<*>, RecipeProcessor<*, *>>

}