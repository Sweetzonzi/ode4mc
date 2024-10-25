package cn.solarmoon.spark_core.registry.common

import cn.solarmoon.spark_core.api.ability.placeable.CustomPlace
import cn.solarmoon.spark_core.api.animation.sync.WholeModelDataRefraction
import cn.solarmoon.spark_core.api.attachment.counting.CountingDeviceTick
import cn.solarmoon.spark_core.feature.inlay.AnvilInlayModifier
import cn.solarmoon.spark_core.feature.thorns.CounterInjuryEvent
import cn.solarmoon.spark_core.feature.use.UseImpl
import net.neoforged.neoforge.common.NeoForge

object SparkCommonEvents {//

    @JvmStatic
    fun register() {
        add(CountingDeviceTick())
        add(CustomPlace())
        add(CounterInjuryEvent())
        add(AnvilInlayModifier())
        add(UseImpl())
        add(WholeModelDataRefraction())
    }

    private fun add(event: Any) {
        NeoForge.EVENT_BUS.register(event)
    }

}