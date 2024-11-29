package cn.solarmoon.spirit_of_fight.feature.hit

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.entity.attack.AttackHelper
import cn.solarmoon.spark_core.api.entity.attack.clearAttackedData
import cn.solarmoon.spark_core.api.entity.attack.getAttackedData
import cn.solarmoon.spark_core.api.entity.state.getSideOf
import net.minecraft.world.entity.Entity
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent

class HitAnimationApplier {

    @SubscribeEvent
    private fun hurt(event: LivingDamageEvent.Post) {
        val entity = event.entity
        val level = entity.level()
        val sourcePos = event.source.sourcePosition ?: return
        if (entity is IEntityAnimatable<*> && !level.isClientSide) {
            (entity as Entity).getAttackedData()?.let { data ->
                val hitType = data.getHitType() ?: return
                val side = AttackHelper.getBoxSide(entity, data.damageBox)
                val posSide = (entity as Entity).getSideOf(sourcePos)
                val damagedBone = data.damageBone
                if (damagedBone != null) {
                    val hitAnimName = hitType.getHitAnimName(damagedBone, side, posSide) ?: return
                    if (!entity.animData.animationSet.hasAnimation(hitAnimName)) return
                    if (!HitType.isPlayingHitAnim(entity) { it.isInTransition || HitType.isPlayingKnockDownAnim(entity) { !it.isInTransition } }) { // 只能在非过渡阶段以及非倒地动画才能切换受击动画
                        val anim = HitType.ALL_HIT_CONSUME_ANIMATIONS[hitAnimName]
                        anim?.consume(entity)
                        anim?.syncToClient(entity.id)
                        (entity as Entity).clearAttackedData()
                    }
                }
            }
        }
    }

}