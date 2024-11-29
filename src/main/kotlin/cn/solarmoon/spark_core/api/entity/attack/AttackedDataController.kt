package cn.solarmoon.spark_core.api.entity.attack

import cn.solarmoon.spark_core.api.phys.obb.OrientedBoundingBox
import cn.solarmoon.spirit_of_fight.feature.hit.HitType
import cn.solarmoon.spirit_of_fight.feature.hit.setHitType
import net.minecraft.world.entity.animal.IronGolem
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.EntityTickEvent
import org.joml.Vector3f

class AttackedDataController {

    @SubscribeEvent
    private fun entityTick(event: EntityTickEvent.Pre) {
        val entity = event.entity

        if (entity is IronGolem) {
            val box = OrientedBoundingBox(entity.position().add(0.0, 2.0, 0.0).toVector3f(), Vector3f(3f))
            val result = entity.boxAttack(box, "233${entity.id}")
            result.forEach { target ->
                target.getAttackedData()?.setHitType(HitType.entries.random())
                target.hurt(entity.damageSources().mobAttack(entity), 1f)
            }
        }
    }

}