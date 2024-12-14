package cn.solarmoon.spark_core.api.phys

import cn.solarmoon.spark_core.api.phys.thread.PhysLevelTickEvent
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.EntityTickEvent

class BoundingBoneApplier {

    @SubscribeEvent
    private fun entityTick(event: PhysLevelTickEvent.Entity) {
        val entity = event.entity
        entity.getBoundingBones().values.forEach { it.physTick() }
    }

}