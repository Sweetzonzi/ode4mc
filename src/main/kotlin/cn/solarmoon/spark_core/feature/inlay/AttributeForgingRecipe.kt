package cn.solarmoon.spark_core.feature.inlay

import cn.solarmoon.spark_core.api.data.RecipeJsonBuilder
import cn.solarmoon.spark_core.api.entry_builder.common.RecipeBuilder
import cn.solarmoon.spark_core.api.recipe.AttributeData
import cn.solarmoon.spark_core.api.recipe.IConcreteRecipe
import cn.solarmoon.spark_core.api.util.ItemStackUtil.isSameAndSufficient
import cn.solarmoon.spark_core.registry.common.SparkRecipes
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.RecipeSerializer


data class AttributeForgingRecipe(
    val input: Ingredient,
    val material: ItemStack,
    val attributeData: AttributeData,
    val slot: EquipmentSlotGroup,
    val expCost: Long,
    val maxForgeCount: Int
): IConcreteRecipe {

    override val entry: RecipeBuilder.RecipeEntry<*>
        get() = SparkRecipes.ATTRIBUTE_FORGING

    /**
     * @return 是否能无限嵌入该消耗物
     */
    fun isForgingLimitless(): Boolean {
        return maxForgeCount == 0
    }

    /**
     * @param stack 作比较的物品栈
     * @return 输入的物品栈是否足够配方的消耗
     */
    fun isMaterialSufficient(stack: ItemStack): Boolean {
        return if (isForgingLimitless()) stack.`is`(material.item) else isSameAndSufficient(stack, material, false)
    }

    class Serializer: RecipeSerializer<AttributeForgingRecipe> {
        override fun codec(): MapCodec<AttributeForgingRecipe> {
            return RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter{ it.input },
                    ItemStack.OPTIONAL_CODEC.fieldOf("material").forGetter{ it.material },
                    AttributeData.CODEC.fieldOf("attribute").forGetter { it.attributeData },
                    EquipmentSlotGroup.CODEC.fieldOf("slot").forGetter { it.slot },
                    Codec.LONG.fieldOf("cost").forGetter { it.expCost },
                    Codec.INT.fieldOf("max_forging_count").forGetter { it.maxForgeCount }
                ).apply(instance, ::AttributeForgingRecipe)
            }
        }

        override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, AttributeForgingRecipe> {
            return object : StreamCodec<RegistryFriendlyByteBuf, AttributeForgingRecipe> {
                override fun encode(buf: RegistryFriendlyByteBuf, value: AttributeForgingRecipe) {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, value.input)
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, value.material)
                    AttributeData.STREAM_CODEC.encode(buf, value.attributeData)
                    EquipmentSlotGroup.STREAM_CODEC.encode(buf, value.slot)
                    buf.writeLong(value.expCost)
                    buf.writeInt(value.maxForgeCount)
                }

                override fun decode(buf: RegistryFriendlyByteBuf): AttributeForgingRecipe {
                    val input = Ingredient.CONTENTS_STREAM_CODEC.decode(buf)
                    val material = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf)
                    val attributeData = AttributeData.STREAM_CODEC.decode(buf)
                    val slot = EquipmentSlotGroup.STREAM_CODEC.decode(buf)
                    val expCost = buf.readLong()
                    val maxForgeCount = buf.readInt()
                    return AttributeForgingRecipe(input, material, attributeData, slot, expCost, maxForgeCount)
                }
            }
        }
    }

    class JsonBuilder(
        val input: Ingredient,
        val material: ItemStack,
        val attributeData: AttributeData,
        val slot: EquipmentSlotGroup,
        val expCost: Long,
        val maxForgeCount: Int
    ): RecipeJsonBuilder() {
        override fun getResult(): Item = material.item

        override fun save(recipeOutput: RecipeOutput, id: ResourceLocation) {
            recipeOutput.accept(id.withPrefix("attribute_forging/${attributeData.attributeModifier.id.path}/"), AttributeForgingRecipe(input, material, attributeData, slot, expCost, maxForgeCount), null)
        }
    }

}