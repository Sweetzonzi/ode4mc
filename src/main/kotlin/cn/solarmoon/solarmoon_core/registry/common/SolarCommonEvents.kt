package cn.solarmoon.solarmoon_core.registry.common

import cn.solarmoon.solarmoon_core.api.ability.placeable.CustomPlace
import cn.solarmoon.solarmoon_core.api.attachment.counting.CountingDeviceTick
import net.neoforged.neoforge.common.NeoForge

object SolarCommonEvents {

    @JvmStatic
    fun register() {
        add(CountingDeviceTick())
        add(CustomPlace())
    }

    private fun add(event: Any) {
        NeoForge.EVENT_BUS.register(event)
    }

}