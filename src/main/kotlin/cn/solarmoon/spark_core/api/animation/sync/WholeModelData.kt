package cn.solarmoon.spark_core.api.animation.sync

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.anim.AnimationSet
import cn.solarmoon.spark_core.api.animation.model.CommonModel
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

data class WholeModelData(
    val models: MutableMap<ResourceLocation, CommonModel>,
    val animationSets: MutableMap<ResourceLocation, AnimationSet>
): CustomPacketPayload {

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return TYPE
    }

    companion object {
        @JvmStatic
        val TYPE = CustomPacketPayload.Type<WholeModelData>(ResourceLocation.fromNamespaceAndPath(SparkCore.MOD_ID, "whole_model_data"))

        @JvmStatic
        val STREAM_CODEC = StreamCodec.composite(
            CommonModel.ORIGIN_MAP_STREAM_CODEC, WholeModelData::models,
            AnimationSet.ORIGIN_MAP_STREAM_CODEC, WholeModelData::animationSets,
            ::WholeModelData
        )
    }

}