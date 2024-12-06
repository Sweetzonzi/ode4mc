package cn.solarmoon.spark_core.api.animation.anim.auto_anim

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.phys.thread.PhysLevelTickEvent
import cn.solarmoon.spark_core.api.phys.thread.getPhysLevel
import kotlinx.coroutines.launch
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent

class AutoAnimApplier {

    @SubscribeEvent
    private fun entityTick(event: PhysLevelTickEvent.Entity) {
        val entity = event.entity
        if (entity is IEntityAnimatable<*>) {
            entity.autoAnims.forEach {
                if (it is EntityAutoAnim) it.frequencyTick()
            }
        }
    }

    @SubscribeEvent
    private fun entityHit(event: LivingDamageEvent.Post) {
        val entity = event.entity
        entity.getPhysLevel()?.let {
            it.scope.launch {
                if (event.newDamage <= 0) return@launch
                if (entity is IEntityAnimatable<*>) {
                    entity.autoAnims.forEach {
                        if (it is HitAutoAnim) it.onActualHit(event)
                    }
                }
            }
        }
    }

}