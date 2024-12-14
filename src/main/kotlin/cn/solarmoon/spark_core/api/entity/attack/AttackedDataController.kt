package cn.solarmoon.spark_core.api.entity.attack

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.phys.data
import cn.solarmoon.spark_core.api.phys.getBoundingBone
import cn.solarmoon.spark_core.api.phys.setAttacked
import cn.solarmoon.spark_core.api.phys.thread.PhysLevelTickEvent
import cn.solarmoon.spark_core.api.phys.thread.getPhysLevel
import cn.solarmoon.spark_core.api.phys.thread.launch
import cn.solarmoon.spark_core.api.phys.toVec3
import cn.solarmoon.spark_core.registry.common.SparkVisualEffects
import cn.solarmoon.spirit_of_fight.feature.hit.HitType
import cn.solarmoon.spirit_of_fight.feature.hit.setHitType
import kotlinx.coroutines.launch
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
            entity.getBoundingBone("body").boundingGeoms?.let {
                val r = SparkVisualEffects.OBB.getRenderableBox("e")
                r.refresh(it[0])
                r.setColor(Color.red)
            }
            entity.getBoundingBone("body").boundingGeoms?.forEach { it.data().onCollide { o2, buffer ->
                entity.level().addParticle(ParticleTypes.CLOUD, it.position.get0(), it.position.get1(), it.position.get2(), 0.0, 0.1, 0.0)
                val target = it.setAttacked(o2)
                target?.getAttackedData()?.setHitType(HitType.entries.random())
                target?.hurt(entity.damageSources().mobAttack(entity), 1f)
            } }
        }
    }

}