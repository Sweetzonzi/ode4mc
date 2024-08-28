package cn.solarmoon.solarmoon_core.api.data

import cn.solarmoon.solarmoon_core.api.data.element.FoodValue
import cn.solarmoon.solarmoon_core.api.recipe.ChanceResult
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import net.minecraft.core.Holder
import net.minecraft.core.NonNullList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.fluids.FluidStack

object SerializeHelper {

    /**
     * @return 机会物品源列表
     */
    @JvmStatic
    fun readChanceResults(json: JsonObject, id: String): NonNullList<ChanceResult> {
        val results = NonNullList.create<ChanceResult>()
        if (json.has(id)) {
            for (je in GsonHelper.getAsJsonArray(json, id)) {
                results.add(ChanceResult.CODEC.parse(JsonOps.INSTANCE, je).result().orElseThrow { IllegalStateException("Invalid ChanceResult") })
            }
        }
        return results
    }

    /**
     * @return 从buf中读取机会物品源列表
     */
    @JvmStatic
    fun readChanceResults(buffer: RegistryFriendlyByteBuf): NonNullList<ChanceResult> {
        val i = buffer.readVarInt()
        val resultsIn = NonNullList.withSize(i, ChanceResult.EMPTY)
        resultsIn.replaceAll { ChanceResult.STREAM_CODEC.decode(buffer) }
        return resultsIn
    }

    /**
     * 写入机会物品源列表
     */
    @JvmStatic
    fun writeChanceResults(buffer: RegistryFriendlyByteBuf, results: NonNullList<ChanceResult>) {
        buffer.writeVarInt(results.size)
        for (result in results) {
            ChanceResult.STREAM_CODEC.encode(buffer, result)
        }
    }

    @JvmStatic
    fun readFluid(json: JsonObject, ide: String): Fluid {
        var fluid = Fluids.EMPTY
        if (json.has(ide)) {
            val id = GsonHelper.getAsString(GsonHelper.getAsJsonObject(json, ide), "id")
            fluid = BuiltInRegistries.FLUID.get(ResourceLocation.tryParse(id))
        }
        return fluid
    }

    @JvmStatic
    fun readFluid(buffer: RegistryFriendlyByteBuf): Fluid {
        return BuiltInRegistries.FLUID.get(buffer.readResourceLocation())
    }

    @JvmStatic
    fun writeFluid(buffer: RegistryFriendlyByteBuf, fluid: Fluid) {
        buffer.writeResourceLocation(BuiltInRegistries.FLUID.getKey(fluid))
    }

    /**
     * read buffer和write有直接的方法，只有json读取需要这个。
     * @return null时返回空
     */
    @JvmStatic
    fun readFluidStack(json: JsonObject, id: String): FluidStack {
        return FluidStack.OPTIONAL_CODEC.parse(JsonOps.INSTANCE, json.get(id)).result().orElseThrow { IllegalStateException("Invalid FluidStack") }
    }

    /**
     * @return 默认从json读取完整的物品
     */
    @JvmStatic
    fun readItemStack(json: JsonObject, id: String): ItemStack {
        return ItemStack.OPTIONAL_CODEC.parse(JsonOps.INSTANCE, json.get(id)).result().orElseThrow { IllegalStateException("Invalid ItemStack") }
    }

    /**
     * @return 默认读取完整的物品
     */
    @JvmStatic
    fun readItemStack(buf: RegistryFriendlyByteBuf): ItemStack {
        return ItemStack.STREAM_CODEC.decode(buf)
    }

    /**
     * 默认发送完整物品
     */
    @JvmStatic
    fun writeItemStack(buf: RegistryFriendlyByteBuf, stack: ItemStack) {
        ItemStack.STREAM_CODEC.encode(buf, stack)
    }

    @JvmStatic
    fun readItemStacks(json: JsonObject, id: String): List<ItemStack> {
        val stacks: MutableList<ItemStack> = ArrayList()
        if (json.has(id)) {
            for (element in GsonHelper.getAsJsonArray(json, id)) {
                stacks.add(ItemStack.OPTIONAL_CODEC.parse(JsonOps.INSTANCE, element).result().orElseThrow { IllegalStateException("Invalid ItemStack") })
            }
        }
        return stacks
    }

    /**
     * 必须和此类中的write配合使用
     */
    @JvmStatic
    fun readItemStacks(buffer: RegistryFriendlyByteBuf): List<ItemStack> {
        val stacks: MutableList<ItemStack> = ArrayList()
        val itemCount = buffer.readVarInt()
        for (i in 0 until itemCount) {
            stacks.add(readItemStack(buffer))
        }
        return stacks
    }

    /**
     * 必须和此类中的read配合使用
     */
    @JvmStatic
    fun writeItemStacks(buffer: RegistryFriendlyByteBuf, stacks: List<ItemStack>) {
        buffer.writeVarInt(stacks.size)
        for (stack in stacks) {
            writeItemStack(buffer, stack)
        }
    }

    @JvmStatic
    fun readIngredient(json: JsonObject, id: String): Ingredient {
        return Ingredient.CODEC.parse(JsonOps.INSTANCE, json.get(id)).result().orElseThrow { IllegalStateException("Invalid Ingredient") }
    }

    @JvmStatic
    fun readIngredients(json: JsonObject, id: String): List<Ingredient> {
        val ingredients: MutableList<Ingredient> = ArrayList()
        if (json.has(id)) {
            for (element in GsonHelper.getAsJsonArray(json, id)) {
                ingredients.add(Ingredient.CODEC.parse(JsonOps.INSTANCE, element).result().orElseThrow { IllegalStateException("Invalid Ingredient") })
            }
        }
        return ingredients
    }

    @JvmStatic
    fun readIngredients(buffer: RegistryFriendlyByteBuf): List<Ingredient> {
        val ingredients: MutableList<Ingredient> = ArrayList()
        val inCount = buffer.readVarInt()
        for (i in 0 until inCount) {
            ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer))
        }
        return ingredients
    }

    @JvmStatic
    fun writeIngredients(buffer: RegistryFriendlyByteBuf, ingredients: List<Ingredient>) {
        buffer.writeVarInt(ingredients.size)
        for (`in` in ingredients) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, `in`)
        }
    }

    @JvmStatic
    fun readBlock(json: JsonObject, id: String): Block {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse(GsonHelper.getAsString(json, "id")))
    }

    @JvmStatic
    fun readBlock(buf: RegistryFriendlyByteBuf): Block {
        return BuiltInRegistries.BLOCK.get(buf.readResourceLocation())
    }

    @JvmStatic
    fun writeBlock(buf: FriendlyByteBuf, block: Block) {
        buf.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(block))
    }

    @JvmStatic
    fun readVec3(buf: RegistryFriendlyByteBuf): Vec3 {
        val x = buf.readDouble()
        val y = buf.readDouble()
        val z = buf.readDouble()
        return Vec3(x, y, z)
    }

    @JvmStatic
    fun writeVec3(buf: RegistryFriendlyByteBuf, vec3: Vec3) {
        buf.writeDouble(vec3.x)
        buf.writeDouble(vec3.y)
        buf.writeDouble(vec3.z)
    }

    @JvmStatic
    fun readVec3List(buf: RegistryFriendlyByteBuf): List<Vec3> {
        val vec3List: MutableList<Vec3> = ArrayList()
        val size = buf.readVarInt()
        for (i in 0 until size) {
            val vec3 = readVec3(buf)
            vec3List.add(vec3)
        }
        return vec3List
    }

    @JvmStatic
    fun writeVec3List(buf: RegistryFriendlyByteBuf, vec3List: List<Vec3>) {
        buf.writeVarInt(vec3List.size)
        for (vec3 in vec3List) {
            writeVec3(buf, vec3)
        }
    }

    @JvmStatic
    fun readEffect(json: JsonObject): MobEffectInstance {
        val id = ResourceLocation.tryParse(GsonHelper.getAsString(json, "id"))
        val effect = Holder.direct(BuiltInRegistries.MOB_EFFECT.get(id)!!)
        val duration = GsonHelper.getAsInt(json, "duration", 0)
        val amplifier = GsonHelper.getAsInt(json, "amplifier", 0)
        val ambient = GsonHelper.getAsBoolean(json, "ambient", false)
        val visible = GsonHelper.getAsBoolean(json, "visible", true)
        val showIcon = GsonHelper.getAsBoolean(json, "showIcon", true)
        return MobEffectInstance(effect, duration, amplifier, ambient, visible, showIcon)
    }

    @JvmStatic
    fun readEffects(json: JsonObject, id: String): List<MobEffectInstance> {
        val effectInstances: MutableList<MobEffectInstance> = ArrayList()
        if (json.has(id)) {
            for (element in GsonHelper.getAsJsonArray(json, id)) {
                effectInstances.add(readEffect(element.asJsonObject))
            }
        }
        return effectInstances
    }

    @JvmStatic
    fun readFoodValue(json: JsonObject, id: String): FoodValue {
        return FoodValue.OPTIONAL_CODEC.parse(JsonOps.INSTANCE, json.get(id)).result().get()
    }

}