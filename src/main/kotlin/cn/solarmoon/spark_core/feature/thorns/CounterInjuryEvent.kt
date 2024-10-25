package cn.solarmoon.spark_core.feature.thorns

import cn.solarmoon.spark_core.registry.common.SparkAttributes
import cn.solarmoon.spark_core.registry.common.SparkDamageTypes
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent


class CounterInjuryEvent {//

    @SubscribeEvent
    fun counter(event: LivingDamageEvent.Post) {
        val livingBeAttacked = event.entity
        val attribute = livingBeAttacked.getAttribute(SparkAttributes.THORNS)
        val direct = event.source.directEntity
        val indirect = event.source.entity
        val source: DamageSource = SparkDamageTypes.THORNS.get(livingBeAttacked.level(), livingBeAttacked)
        if (attribute != null && attribute.value > 0 && event.source.type() !== source.type()) {
            if (direct is LivingEntity) {
                direct.hurt(source, attribute.value.toFloat())
                direct.invulnerableTime = 0 //使得荆棘也能同时生效
            }
        }
    }

}