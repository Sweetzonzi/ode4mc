package cn.solarmoon.spark_core.data

import cn.solarmoon.spark_core.api.data.element.AttributeData
import cn.solarmoon.spark_core.feature.inlay.AttributeForgingRecipe
import cn.solarmoon.spark_core.registry.common.SparkAttributes
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient
import net.neoforged.neoforge.common.Tags
import java.util.concurrent.CompletableFuture

class RecipeDataProvider(output: PackOutput, registries: CompletableFuture<HolderLookup.Provider>) : RecipeProvider(output, registries) {

    override fun buildRecipes(recipeOutput: RecipeOutput) {
        AttributeForgingRecipe.JsonBuilder {
            AttributeForgingRecipe(
                Ingredient.of(Tags.Items.ARMORS),
                ItemStack(Items.CACTUS),
                AttributeData.create(SparkAttributes.THORNS.get(), 1.0, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.ARMOR,
                3,
                1)
        }.save(recipeOutput)
    }

}