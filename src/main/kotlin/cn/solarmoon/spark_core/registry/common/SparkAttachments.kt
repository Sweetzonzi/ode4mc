package cn.solarmoon.spark_core.registry.common

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.attachment.animation.AnimTicker
import cn.solarmoon.spark_core.api.attachment.counting.CountingDevice


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

}