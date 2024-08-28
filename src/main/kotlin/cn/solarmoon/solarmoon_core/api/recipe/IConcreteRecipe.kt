package cn.solarmoon.solarmoon_core.api.recipe

import net.minecraft.core.HolderLookup
import net.minecraft.core.RegistryAccess
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.neoforged.neoforge.items.wrapper.RecipeWrapper

/**
 * 若配方交互无需gui，则可用此接口
 */
interface IConcreteRecipe: Recipe<RecipeWrapper> {

    override fun matches(inv: RecipeWrapper, level: Level): Boolean {
        return false
    }

    override fun getResultItem(registries: HolderLookup.Provider): ItemStack {
        return ItemStack.EMPTY
    }

    override fun assemble(inv: RecipeWrapper, registries: HolderLookup.Provider): ItemStack {
        return ItemStack.EMPTY
    }

    override fun canCraftInDimensions(width: Int, height: Int): Boolean {
        return true
    }

}