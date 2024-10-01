package cn.solarmoon.spark_core.registry.common

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.attachment.animation.AnimTicker
import cn.solarmoon.spark_core.api.attachment.counting.CountingDevice
import cn.solarmoon.spark_core.api.animation.anim.AnimationSet
import cn.solarmoon.spark_core.api.animation.anim.ClientAnimData
import cn.solarmoon.spark_core.api.animation.model.CommonModel


object SparkAttachments {
    @JvmStatic
    fun register() {}

    @JvmStatic
    val ANIMTICKER = SparkCore.REGISTER.attachment<AnimTicker>()
        .id("animticker")
        .defaultValue { AnimTicker() }
        .serializer { builder -> builder.serialize(AnimTicker.CODEC) }
        .build()

    @JvmStatic
    val COUNTING_DEVICE = SparkCore.REGISTER.attachment<CountingDevice>()
        .id("counting_device")
        .defaultValue { CountingDevice() }
        .serializer{ builder -> builder.serialize(CountingDevice.CODEC) }
        .build()

    @JvmStatic
    val ANIM_DATA = SparkCore.REGISTER.attachment<ClientAnimData>()
        .id("anim_data")
        .defaultValue { ClientAnimData.EMPTY }
        .serializer { it.serialize(ClientAnimData.CODEC) }
        .build()

}