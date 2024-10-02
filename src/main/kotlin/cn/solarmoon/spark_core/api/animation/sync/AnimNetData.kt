package cn.solarmoon.spark_core.api.animation.sync

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.anim.AnimData
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

data class AnimNetData(
    val entityId: Int,
    val animData: AnimData,
    /**
     * 这一项决定了是否需要在任何条件下都替换动画数据，一般情况下，动画数据只会当两个播放的动画不一致的时候才会更新，但是如果此项为ture，哪怕tick之间有些微的差距也会完全替换
     */
    val placeAnyCase: Boolean = false
): CustomPacketPayload {

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return TYPE
    }

    companion object {
        @JvmStatic
        val TYPE = CustomPacketPayload.Type<AnimNetData>(ResourceLocation.fromNamespaceAndPath(SparkCore.MOD_ID, "animation_network_data"))

        @JvmStatic
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, AnimNetData::entityId,
            AnimData.STREAM_CODEC, AnimNetData::animData,
            ByteBufCodecs.BOOL, AnimNetData::placeAnyCase,
            ::AnimNetData
        )
    }

}
