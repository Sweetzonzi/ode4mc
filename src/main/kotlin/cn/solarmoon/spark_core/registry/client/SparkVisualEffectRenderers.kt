package cn.solarmoon.spark_core.registry.client

import cn.solarmoon.aurorian2_bosses_reborn.client.visual_effect.TrailRenderer
import cn.solarmoon.spark_core.api.visual_effect.VisualEffectManager
import cn.solarmoon.spark_core.api.visual_effect.VisualEffectRenderer
import cn.solarmoon.spark_core.api.visual_effect.common.StreakRenderer

object SparkVisualEffectRenderers {

    @JvmStatic
    private fun add(renderer: VisualEffectRenderer) {
        VisualEffectManager.registerRenderer(renderer)
    }

    @JvmStatic
    fun register() {
        add(TrailRenderer())
        add(StreakRenderer())
    }

}