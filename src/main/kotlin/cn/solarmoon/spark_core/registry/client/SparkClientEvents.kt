package cn.solarmoon.spark_core.registry.client

import cn.solarmoon.spark_core.api.visual_effect.VisualEffectRenderer
import cn.solarmoon.spark_core.api.visual_effect.VisualEffectTicker
import net.neoforged.neoforge.common.NeoForge

object SparkClientEvents {

    @JvmStatic
    fun register() {
        add(VisualEffectTicker())
    }

    private fun add(event: Any) {
        NeoForge.EVENT_BUS.register(event)
    }

}