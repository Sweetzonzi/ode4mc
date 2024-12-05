package cn.solarmoon.spark_core.api.animation.anim.play

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.EntityTickEvent

class AnimTicker {

    @SubscribeEvent
    private fun entityTick(event: EntityTickEvent.Pre) {
        val entity = event.entity
        val level = entity.level()
        if (entity is IEntityAnimatable<*>) {
            // 基本tick
            entity.animController.animTick()
        }
    }

}