package cn.solarmoon.spark_core.registry.client

import cn.solarmoon.spark_core.api.phys.obb.renderable.OBBRenderer
import cn.solarmoon.spark_core.api.visual_effect.common.shadow.ShadowRenderer
import cn.solarmoon.spark_core.api.visual_effect.common.trail.TrailRenderer

object SparkVisualEffectRenderers {

    @JvmStatic
    val OBB = OBBRenderer()

    @JvmStatic
    val TRAIL = TrailRenderer()

    @JvmStatic
    val SHADOW = ShadowRenderer()

    @JvmStatic
    fun register() {}

}