package cn.solarmoon.spark_core.compat.jei.category

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.cap.item.ItemStackHandlerHelper
import cn.solarmoon.spark_core.api.compat.jei.BaseJEICategory
import cn.solarmoon.spark_core.feature.inlay.AttributeForgingRecipe
import cn.solarmoon.spark_core.feature.inlay.InlayHelper
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder
import mezz.jei.api.gui.ingredient.IRecipeSlotsView
import mezz.jei.api.helpers.IGuiHelper
import mezz.jei.api.recipe.IFocusGroup
import mezz.jei.api.recipe.RecipeIngredientRole
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack


class AttributeForgingCategory(helper: IGuiHelper) : BaseJEICategory<AttributeForgingRecipe>(helper) {//

    override fun setRecipe(builder: IRecipeLayoutBuilder, recipe: AttributeForgingRecipe, focuses: IFocusGroup) {
        val leftInputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).addIngredients(recipe.input)
        val rightInputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 43, 1).addItemStack(recipe.material)
        val stacks: MutableList<ItemStack> = ArrayList()
        for (`in` in recipe.input.getItems()) {
            val copy: ItemStack = `in`.copy()
            val handler = InlayHelper.getInlayHandler(copy)
            ItemStackHandlerHelper.insertItem(handler, recipe.material) // 插入材料
            InlayHelper.addAttributeToItem(copy, recipe) // 给属性
            stacks.add(copy)
        }
        val outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 1).addItemStacks(stacks)

        if (recipe.input.getItems().size == stacks.size) {
            builder.createFocusLink(leftInputSlot, outputSlot)
        }
    }

    override fun draw(
        recipe: AttributeForgingRecipe,
        recipeSlotsView: IRecipeSlotsView,
        guiGraphics: GuiGraphics,
        mouseX: Double,
        mouseY: Double
    ) {
        helper.slotDrawable.draw(guiGraphics, 0, 0)
        helper.slotDrawable.draw(guiGraphics, 42, 0)
        helper.slotDrawable.draw(guiGraphics, 94, 0)
        PLUS.draw(guiGraphics, 23, 2)
        EMPTY_ARROW.draw(guiGraphics, 66, 1)
        if (mouseX in 66.0..88.0 && mouseY >= 1 && mouseY <= 17) {
            val components: MutableList<Component> = ArrayList<Component>()
            components.add(Component.literal(""))
            components.add(
                SparkCore.TRANSLATOR.set(
                    "jei", "attribute_forging.max_forging_count",
                    ChatFormatting.WHITE, recipe.maxForgeCount
                )
            )
            val expX = mouseX.toInt() + 12
            val expY = mouseY.toInt() - 11
            val poseStack = guiGraphics.pose()
            poseStack.pushPose()
            poseStack.translate(0f, 0f, 1000f)
            EXP.draw(guiGraphics, expX, expY)
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.literal("x" + recipe.expCost).withStyle(ChatFormatting.GREEN),
                expX + 9, expY, 0xFFFFFF
            )
            poseStack.popPose()
            guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, components, mouseX.toInt(), mouseY.toInt())
        }
    }

}