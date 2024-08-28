package cn.solarmoon.solarmoon_core.api.recipe

import cn.solarmoon.solarmoon_core.api.util.TextUtil.splitFromColon
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import java.util.*
import java.util.stream.Stream

data class AttributeData(val attribute: Attribute, val attributeModifier: AttributeModifier ) {

    companion object {
        @JvmStatic
        val CODEC: Codec<AttributeData> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("id").forGetter { BuiltInRegistries.ATTRIBUTE.getKey(it.attribute).toString() },
                Codec.DOUBLE.fieldOf("value").forGetter { it.attributeModifier.amount },
                Codec.STRING.optionalFieldOf("operation", "addition").forGetter { it.attributeModifier.operation.name }
            ).apply(instance) { id, value, op ->
                val attribute = BuiltInRegistries.ATTRIBUTE.get(ResourceLocation.tryParse(id))
                    ?: throw RuntimeException("Unknown attribute id. Please check your Recipe data.")
                val operation = when (op) {
                    "addition" -> AttributeModifier.Operation.ADD_VALUE
                    "multiply_base" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    "multiply_total" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    else -> throw RuntimeException("Unknown attribute operation, there are only 'addition', 'multiply_base' and 'multiply_total' can be chosen.")
                }
                val modifier = AttributeModifier(ResourceLocation.tryParse(id)!!, value, operation)
                AttributeData(attribute, modifier)
            }
        }

        @JvmStatic
        val STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, AttributeData> {
            override fun encode(buffer: RegistryFriendlyByteBuf, value: AttributeData) {
                buffer.writeUtf(BuiltInRegistries.ATTRIBUTE.getKey(value.attribute).toString())
                buffer.writeDouble(value.attributeModifier.amount)
                buffer.writeEnum(value.attributeModifier.operation)
            }

            override fun decode(buffer: RegistryFriendlyByteBuf): AttributeData {
                val id = buffer.readUtf()
                val value = buffer.readDouble()
                val operation = buffer.readEnum(AttributeModifier.Operation::class.java)

                val attribute = BuiltInRegistries.ATTRIBUTE.get(ResourceLocation.tryParse(id))
                val modifier = AttributeModifier(ResourceLocation.tryParse(id)!!, value, operation)
                return AttributeData(attribute!!, modifier)
            }
        }
    }

}