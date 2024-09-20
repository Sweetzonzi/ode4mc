package cn.solarmoon.spark_core.api.data.element

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier

data class AttributeData(val attribute: Attribute, val attributeModifier: AttributeModifier ) {

    val holder: Holder.Reference<Attribute>
        get() = BuiltInRegistries.ATTRIBUTE.getHolder(BuiltInRegistries.ATTRIBUTE.getKey(attribute)!!).get()

    companion object {
        @JvmStatic
        fun create(attribute: Attribute, value: Double, operation: AttributeModifier.Operation): AttributeData {
            val id = BuiltInRegistries.ATTRIBUTE.getKey(attribute).toString()
            return AttributeData(attribute, AttributeModifier(ResourceLocation.tryParse(id)!!, value, operation))
        }

        @JvmStatic
        val CODEC: Codec<AttributeData> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("id").forGetter { BuiltInRegistries.ATTRIBUTE.getKey(it.attribute).toString() },
                Codec.DOUBLE.fieldOf("amount").forGetter { it.attributeModifier.amount },
                Codec.STRING.optionalFieldOf("operation", "add_value").forGetter { it.attributeModifier.operation.name.lowercase() }
            ).apply(instance) { id, value, op ->
                val attribute = BuiltInRegistries.ATTRIBUTE.get(ResourceLocation.tryParse(id))
                    ?: throw RuntimeException("Unknown attribute id. Please check your Recipe data.")
                val operation = when (op.lowercase()) {
                    "add_value" -> AttributeModifier.Operation.ADD_VALUE
                    "add_multiplied_base" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    "add_multiplied_total" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
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