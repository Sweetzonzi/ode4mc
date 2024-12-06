package cn.solarmoon.spirit_of_fight.feature.fight_skill.skill

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.animation.sync.SyncedAnimation
import cn.solarmoon.spark_core.api.entity.state.getLateralSide
import cn.solarmoon.spark_core.api.util.Side
import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.FightSkillController
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

abstract class ParryAnimSkill(
    controller: FightSkillController,
    val anim: SyncedAnimation,
    guardRange: Double
): GuardAnimSkill(controller, setOf(anim.anim.name), guardRange) {

    companion object {
        @JvmStatic
        fun createParrySyncedAnim(prefix: String) = SyncedAnimation(MixedAnimation("$prefix:parry", startTransSpeed = 10f))

        @JvmStatic
        val PARRY_SYNCED_ANIM = buildMap {
            listOf(Side.LEFT, Side.RIGHT).forEach {
                put(it, SyncedAnimation(MixedAnimation("parried_$it", startTransSpeed = 10f)))
            }
        }

        @JvmStatic
        fun registerAnim() {}
    }

    override fun getBoxId(index: Int): String {
        return "${entity.id}:parry"
    }

    override fun start(sync: (SyncedAnimation) -> Unit) {
        anim.consume(animatable)
        sync.invoke(anim)
    }

    override fun onSuccessGuard(
        attackerPos: Vec3,
        damageSource: DamageSource,
        value: Float,
        anim: MixedAnimation
    ): Boolean {
        damageSource.entity?.let {
            parry(attackerPos, it)
        }
        return false
    }

    fun parry(attackerPos: Vec3, attacker: Entity) {
        if (attacker is IEntityAnimatable<*> && !attacker.level().isClientSide) {
            val side = entity.getLateralSide(attackerPos, true)
            if (attacker.animData.animationSet.hasAnimation("parried_$side")) {
                PARRY_SYNCED_ANIM[side]?.let { anim ->
                    anim.consume(attacker)
                    anim.syncToClient(attacker.id)
                }
            }
        }
    }

}