package cn.solarmoon.spark_core.api.phys

import cn.solarmoon.spark_core.api.phys.thread.getPhysWorld
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent
import net.neoforged.neoforge.event.tick.EntityTickEvent

class BoundingBoneApplier {

    @SubscribeEvent
    private fun entityTick(event: EntityTickEvent.Pre) {
        val entity = event.entity
        entity.getBoundingBones().values.forEach { it.tick() }
    }

    @SubscribeEvent
    private fun onEntityRemove(event: EntityLeaveLevelEvent) {
        val entity = event.entity
        entity.getPhysWorld().delayActions.add {
            entity.getBoundingBones().values.forEach { it.body.destroy() }
        }
    }

}