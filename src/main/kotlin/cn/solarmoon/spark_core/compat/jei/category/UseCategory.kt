package cn.solarmoon.spark_core.compat.jei.category

import cn.solarmoon.spark_core.api.compat.jei.BaseJEICategory
import cn.solarmoon.spark_core.api.recipe.ChanceResult
import cn.solarmoon.spark_core.feature.use.UseRecipe
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder
import mezz.jei.api.gui.ingredient.IRecipeSlotsView
import mezz.jei.api.helpers.IGuiHelper
import mezz.jei.api.recipe.IFocusGroup
import mezz.jei.api.recipe.RecipeIngredientRole
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.NonNullList
import net.minecraft.world.item.ItemStack


class UseCategory(helper: IGuiHelper) : BaseJEICategory<UseRecipe>(helper) {

    override fun setRecipe(builder: IRecipeLayoutBuilder, recipe: UseRecipe, focuses: IFocusGroup) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).addIngredients(recipe.ingredient)
        builder.addSlot(RecipeIngredientRole.INPUT, 43, 1).addItemStack(ItemStack(recipe.inputBlock.asItem()))
        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 1).addItemStack(ItemStack(recipe.outputBlock.asItem()))
        //val recipeOutputs: NonNullList<ChanceResult> = recipe.chanceResults 还没加额外物品的内容，后续再说
    }

    override fun draw(recipe: UseRecipe, recipeSlotsView: IRecipeSlotsView, guiGraphics: GuiGraphics, mouseX: Double, mouseY: Double) {
        DEFAULT_SLOT.draw(guiGraphics, 94, 0)
        EMPTY_ARROW.draw(guiGraphics, 66, 1)
        HAND_POINT.draw(guiGraphics, 23, 3)
    }

}