package cn.solarmoon.spark_core.api.visual_effect

import cn.solarmoon.spark_core.api.phys.thread.ClientPhysLevel
import cn.solarmoon.spark_core.api.phys.thread.PhysLevelTickEvent
import cn.solarmoon.spark_core.api.visual_effect.VisualEffectRenderer.Companion.ALL_VISUAL_EFFECTS
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.client.event.ClientTickEvent

class VisualEffectTicker {

    @SubscribeEvent
    private fun tick(event: PhysLevelTickEvent) {
        if (event.level is ClientPhysLevel) {
            ALL_VISUAL_EFFECTS.forEach { it.tick() }
        }
    }

}