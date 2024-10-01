package cn.solarmoon.spark_core.api.phys.collision

import cn.solarmoon.spark_core.SparkCore
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

data class FreeCollisionBoxData(
    val id: String,
    val color: Int,
    val lifetime: Int,
    val box: FreeCollisionBox
): CustomPacketPayload {

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return TYPE
    }

    companion object {
        @JvmStatic
        val TYPE = CustomPacketPayload.Type<FreeCollisionBoxData>(ResourceLocation.fromNamespaceAndPath(SparkCore.MOD_ID, "box"))

        @JvmStatic
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, FreeCollisionBoxData::id,
            ByteBufCodecs.INT, FreeCollisionBoxData::color,
            ByteBufCodecs.INT, FreeCollisionBoxData::lifetime,
            FreeCollisionBox.STREAM_CODEC, FreeCollisionBoxData::box,
            ::FreeCollisionBoxData
        )
    }

}