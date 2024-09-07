package cn.solarmoon.spark_core.feature.use

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.data.RecipeJsonBuilder
import cn.solarmoon.spark_core.api.data.SerializeHelper
import cn.solarmoon.spark_core.api.entry_builder.common.RecipeBuilder
import cn.solarmoon.spark_core.api.recipe.ChanceResult
import cn.solarmoon.spark_core.api.recipe.IConcreteRecipe
import cn.solarmoon.spark_core.registry.common.SparkRecipes
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.block.Block
import test.b

data class UseRecipe(
    val ingredient: Ingredient,
    val inputBlock: Block,
    val outputBlock: Block,
    val chanceResults: List<ChanceResult>
): IConcreteRecipe {

    override val entry: RecipeBuilder.RecipeEntry<*> = SparkRecipes.USE

    class Serializer: RecipeSerializer<UseRecipe> {
        override fun codec(): MapCodec<UseRecipe> {
            return RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter{ it.ingredient },
                    SerializeHelper.BLOCK.CODEC.fieldOf("input_block").forGetter { it.inputBlock },
                    SerializeHelper.BLOCK.CODEC.fieldOf("output_block").forGetter { it.outputBlock },
                    ChanceResult.LIST_CODEC.fieldOf("results").forGetter { it.chanceResults }
                ).apply(instance, ::UseRecipe)
            }
        }

        override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, UseRecipe> {
            return object : StreamCodec<RegistryFriendlyByteBuf, UseRecipe> {
                override fun decode(buffer: RegistryFriendlyByteBuf): UseRecipe {
                    val ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer)
                    val inputBlock = SerializeHelper.BLOCK.STREAM_CODEC.decode(buffer)
                    val outputBlock = SerializeHelper.BLOCK.STREAM_CODEC.decode(buffer)
                    val chanceResults = ChanceResult.LIST_STREAM_CODEC.decode(buffer)
                    return UseRecipe(ingredient, inputBlock, outputBlock, chanceResults)
                }

                override fun encode(buffer: RegistryFriendlyByteBuf, value: UseRecipe) {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, value.ingredient)
                    SerializeHelper.BLOCK.STREAM_CODEC.encode(buffer, value.inputBlock)
                    SerializeHelper.BLOCK.STREAM_CODEC.encode(buffer, value.outputBlock)
                    ChanceResult.LIST_STREAM_CODEC.encode(buffer, value.chanceResults)
                }
            }
        }
    }

    class JsonBuilder(
        val ingredient: Ingredient,
        val inputBlock: Block,
        val outputBlock: Block,
        val chanceResults: List<ChanceResult>
    ): RecipeJsonBuilder() {
        override fun getResult(): Item {
            return outputBlock.asItem()
        }

        override fun save(recipeOutput: RecipeOutput, id: ResourceLocation) {
            recipeOutput.accept(id.withPrefix("use/"), UseRecipe(ingredient, inputBlock, outputBlock, chanceResults), null)
        }
    }

}