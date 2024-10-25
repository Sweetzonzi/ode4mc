package cn.solarmoon.spark_core.api.animation.sync

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.anim.play.AnimData
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

data class AnimNetData(
    val id: Int,
    val animData: AnimData,
): CustomPacketPayload {

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return TYPE
    }

    companion object {
        @JvmStatic
        val TYPE = CustomPacketPayload.Type<AnimNetData>(ResourceLocation.fromNamespaceAndPath(SparkCore.MOD_ID, "animation_network_data"))

        @JvmStatic
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, AnimNetData::id,
            AnimData.STREAM_CODEC, AnimNetData::animData,
            ::AnimNetData
        )
    }

}
