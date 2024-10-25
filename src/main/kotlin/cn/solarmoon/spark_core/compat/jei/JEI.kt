package cn.solarmoon.spark_core.compat.jei

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.compat.jei.BaseJEI
import cn.solarmoon.spark_core.compat.jei.category.AttributeForgingCategory
import cn.solarmoon.spark_core.compat.jei.category.UseCategory
import cn.solarmoon.spark_core.feature.inlay.AttributeForgingRecipe
import cn.solarmoon.spark_core.feature.use.UseRecipe
import cn.solarmoon.spark_core.registry.client.SparkResources
import cn.solarmoon.spark_core.registry.common.SparkRecipes
import mezz.jei.api.JeiPlugin
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient


@JeiPlugin
class JEI: BaseJEI() {//

    override fun register() {
        add(
            builder()
                .boundCategory(AttributeForgingCategory(guiHelper))
                .recipeType(SparkRecipes.ATTRIBUTE_FORGING.type.get())
                .addRecipeCatalyst(Ingredient.of(ItemTags.ANVIL))
                .icon(Items.ANVIL)
                .emptyBackground(112, 18)
                .title(SparkCore.TRANSLATOR.set("jei", "attribute_forging.title"))
                .build("attribute_forging", AttributeForgingRecipe::class.java),
            builder()
                .boundCategory(UseCategory(guiHelper))
                .recipeType(SparkRecipes.USE.type.get())
                .icon(guiHelper.createDrawable(SparkResources.JEI_HAND_POINT, 0, 0, 14, 11))
                .emptyBackground(112, 18)
                .title(SparkCore.TRANSLATOR.set("jei", "use.title"))
                .build("use", UseRecipe::class.java)
        )
    }

    override fun getModId(): String = SparkCore.MOD_ID

    override fun getPluginUid(): ResourceLocation = ResourceLocation.tryParse(modId)!!

}