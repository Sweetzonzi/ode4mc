package cn.solarmoon.spark_core.api.recipe

import com.google.gson.JsonObject
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.util.GsonHelper
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.neoforged.neoforge.common.util.RecipeMatcher
import java.util.function.Consumer

data class ProportionalIngredient(val ingredient: Ingredient, val count: Int) {

    companion object {

        @JvmStatic
        val CODEC: Codec<ProportionalIngredient> = RecordCodecBuilder.create { instance ->
            instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter { it.ingredient },
                Codec.INT.fieldOf("count").forGetter { it.count }
            ).apply(instance, ::ProportionalIngredient)
        }

        @JvmStatic
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ProportionalIngredient> = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, ProportionalIngredient::ingredient,
            ByteBufCodecs.INT, ProportionalIngredient::count,
            ::ProportionalIngredient
        )

        @JvmStatic
        fun readProportionalIngredients(json: JsonObject, id: String): List<ProportionalIngredient> {
            val ingredients: MutableList<ProportionalIngredient> = ArrayList()
            if (json.has(id)) {
                for (element in GsonHelper.getAsJsonArray(json, id)) {
                    val j = element.asJsonObject
                    ingredients.add(CODEC.parse(JsonOps.INSTANCE, j).result().orElseThrow { IllegalArgumentException("Invalid ingredient JSON") })
                }
            }
            return ingredients
        }

        @JvmStatic
        fun readProportionalIngredients(buf: RegistryFriendlyByteBuf): List<ProportionalIngredient> {
            val ingredients: MutableList<ProportionalIngredient> = ArrayList()
            val inCount = buf.readVarInt()

            for (i in 0 until inCount) {
                ingredients.add(STREAM_CODEC.decode(buf))
            }

            return ingredients
        }

        @JvmStatic
        fun writeProportionalIngredients(buf: RegistryFriendlyByteBuf, ingredients: List<ProportionalIngredient>) {
            buf.writeVarInt(ingredients.size)
            for (proportionalIngredient in ingredients) {
                STREAM_CODEC.encode(buf, proportionalIngredient)
            }
        }

        @JvmStatic
        fun getAllIngredients(proportionalIngredients: List<ProportionalIngredient>): List<Ingredient> {
            val ingredients: MutableList<Ingredient> = ArrayList()
            proportionalIngredients.forEach(Consumer { p: ProportionalIngredient -> ingredients.add(p.ingredient) })
            return ingredients
        }

        @JvmStatic
        fun sumCount(proportionalIngredients: List<ProportionalIngredient>): Int {
            return proportionalIngredients.stream().mapToInt { p: ProportionalIngredient -> p.count }.sum()
        }

        @JvmStatic
        fun getAccess(`in`: ProportionalIngredient, proportionalIngredients: List<ProportionalIngredient>): Double {
            return `in`.count.toDouble() / sumCount(proportionalIngredients)
        }

        @JvmStatic
        fun findMatch(stacks: List<ItemStack>, ins: List<ProportionalIngredient>): Pair<Boolean, Int> {
            val match: MutableMap<Item, Int> = HashMap()
            // 把所有种类相同的物品堆到一起
            stacks.forEach(Consumer { stack: ItemStack ->
                match[stack.item] = match.getOrDefault(stack.item, 0) + stack.count
            })
            val ingredients: List<Ingredient> = getAllIngredients(ins)
            val stackMatch = match.keys.stream().map { item -> ItemStack(item) }.toList()
            val typeMatch = RecipeMatcher.findMatches(stackMatch, ingredients) != null

            val itemSum = match.values.stream().mapToInt { obj: Int -> obj }.sum()
            var proportionMatch = false
            for ((key, value) in match) {
                for (pi in ins) {
                    if (pi.ingredient.test(ItemStack(key))) {
                        val access = value.toDouble() / itemSum
                        proportionMatch = getAccess(pi, ins) == access
                        if (!proportionMatch) {
                            break
                        }
                    }
                }
                if (!proportionMatch) {
                    break
                }
            }

            val singleAccessMatch = itemSum % sumCount(ins) == 0

            return Pair.of(typeMatch && proportionMatch && singleAccessMatch, itemSum / sumCount(ins))
        }

    }

}
