package cn.solarmoon.solarmoon_core.api.recipe.processor

import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeType
import net.neoforged.neoforge.items.wrapper.RecipeWrapper

/**
 * 纯空（无json）配方类型，用于在processor中作为识别符，无其他用处
 */
data class EmptyRecipeType(val processorClass :Class<out RecipeProcessor<*, *>>): RecipeType<Recipe<RecipeWrapper>> {

}