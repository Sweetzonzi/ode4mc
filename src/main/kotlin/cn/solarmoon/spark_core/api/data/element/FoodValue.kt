package cn.solarmoon.spark_core.api.data.element

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

data class FoodValue(val nutrition: Int, val saturation: Float) {

    companion object {
        @JvmStatic
        val EMPTY = FoodValue(0, 0f)

        @JvmStatic
        val OPTIONAL_CODEC: Codec<FoodValue> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.fieldOf("nutrition").orElse(0).forGetter { it.nutrition },
                Codec.FLOAT.fieldOf("saturation").orElse(0f).forGetter { it.saturation }
            ).apply(instance, ::FoodValue)
        }

        @JvmStatic
        val OPTIONAL_STREAM_CODEC: StreamCodec<ByteBuf, FoodValue> = StreamCodec.composite(
            ByteBufCodecs.INT, FoodValue::nutrition,
            ByteBufCodecs.FLOAT, FoodValue::saturation,
            ::FoodValue
        )
    }

    fun isValid(): Boolean {
        return nutrition > 0 || saturation > 0
    }

}
