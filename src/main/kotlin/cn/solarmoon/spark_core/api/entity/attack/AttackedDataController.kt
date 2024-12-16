package cn.solarmoon.spark_core.api.entity.attack

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.phys.data
import cn.solarmoon.spark_core.api.phys.getBoundingBone
import cn.solarmoon.spark_core.api.phys.livingCommonAttack
import cn.solarmoon.spark_core.api.phys.setAttacked
import cn.solarmoon.spark_core.registry.common.SparkVisualEffects
import cn.solarmoon.spirit_of_fight.feature.hit.HitType
import cn.solarmoon.spirit_of_fight.feature.hit.setHitType
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.animal.IronGolem
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.EntityTickEvent
import java.awt.Color

class AttackedDataController {

    @SubscribeEvent
    private fun entityTick(event: EntityTickEvent.Pre) {
        val entity = event.entity

        if (entity is IronGolem) {
            val geom = entity.getBoundingBone("body").geom
            geom.data().onCollide { o2, buffer ->
                entity.level().addParticle(ParticleTypes.CLOUD, geom.position.get0(), geom.position.get1(), geom.position.get2(), 0.0, 0.1, 0.0)
                val target = geom.livingCommonAttack(o2, false)
                target?.getAttackedData()?.setHitType(HitType.entries.random())
                geom.data().attackedEntities.clear()
            }
        }
    }

}