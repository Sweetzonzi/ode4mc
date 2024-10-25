package cn.solarmoon.spark_core.feature.use

import cn.solarmoon.spark_core.api.data.RecipeJsonBuilder
import cn.solarmoon.spark_core.api.data.SerializeHelper
import cn.solarmoon.spark_core.api.data.element.ChanceResult
import cn.solarmoon.spark_core.api.entry_builder.common.RecipeBuilder
import cn.solarmoon.spark_core.api.recipe.IConcreteRecipe
import cn.solarmoon.spark_core.registry.common.SparkRecipes
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.block.Block

data class UseRecipe(
    val ingredient: Ingredient,
    val inputBlock: Block,
    val outputBlock: Block,
    val chanceResults: List<ChanceResult>
): IConcreteRecipe {//

    override val entry: RecipeBuilder.RecipeEntry<*> = SparkRecipes.USE

    class Serializer: RecipeSerializer<UseRecipe> {
        override fun codec(): MapCodec<UseRecipe> {
            return RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter{ it.ingredient },
                    SerializeHelper.BLOCK_CODEC.fieldOf("input_block").forGetter { it.inputBlock },
                    SerializeHelper.BLOCK_CODEC.fieldOf("output_block").forGetter { it.outputBlock },
                    ChanceResult.LIST_CODEC.fieldOf("results").forGetter { it.chanceResults }
                ).apply(instance, ::UseRecipe)
            }
        }

        override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, UseRecipe> {
            return object : StreamCodec<RegistryFriendlyByteBuf, UseRecipe> {
                override fun decode(buffer: RegistryFriendlyByteBuf): UseRecipe {
                    val ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer)
                    val inputBlock = SerializeHelper.BLOCK_STREAM_CODEC.decode(buffer)
                    val outputBlock = SerializeHelper.BLOCK_STREAM_CODEC.decode(buffer)
                    val chanceResults = ChanceResult.LIST_STREAM_CODEC.decode(buffer)
                    return UseRecipe(ingredient, inputBlock, outputBlock, chanceResults)
                }

                override fun encode(buffer: RegistryFriendlyByteBuf, value: UseRecipe) {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, value.ingredient)
                    SerializeHelper.BLOCK_STREAM_CODEC.encode(buffer, value.inputBlock)
                    SerializeHelper.BLOCK_STREAM_CODEC.encode(buffer, value.outputBlock)
                    ChanceResult.LIST_STREAM_CODEC.encode(buffer, value.chanceResults)
                }
            }
        }
    }

    class JsonBuilder(val use: () -> UseRecipe): RecipeJsonBuilder() {
        override val name: ResourceLocation
            get() = BuiltInRegistries.BLOCK.getKey(use.invoke().outputBlock)

        override val prefix: String
            get() = SparkRecipes.USE.type.id.path

        override fun getRecipe(recipeOutput: RecipeOutput, location: ResourceLocation): Recipe<*> {
            return use.invoke()
        }
    }

}