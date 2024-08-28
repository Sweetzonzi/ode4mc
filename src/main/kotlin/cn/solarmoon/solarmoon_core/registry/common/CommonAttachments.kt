package cn.solarmoon.solarmoon_core.registry.common

import cn.solarmoon.solarmoon_core.SolarMoonCore
import cn.solarmoon.solarmoon_core.api.attachment.animation.AnimTicker
import cn.solarmoon.solarmoon_core.api.attachment.counting.CountingDevice


object CommonAttachments {
    @JvmStatic
    fun register() {}

    val ANIMTICKER = SolarMoonCore.REGISTER.attachment<AnimTicker>()
        .id("animticker")
        .defaultValue { AnimTicker() }
        .serializer(AnimTicker.CODEC)
        .build()

    val COUNTING_DEVICE = SolarMoonCore.REGISTER.attachment<CountingDevice>()
        .id("counting_device")
        .defaultValue { CountingDevice() }
        .serializer(CountingDevice.CODEC)
        .build()

}