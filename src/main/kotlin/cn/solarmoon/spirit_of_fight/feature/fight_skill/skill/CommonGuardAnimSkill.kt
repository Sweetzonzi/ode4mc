package cn.solarmoon.spirit_of_fight.feature.fight_skill.skill

import cn.solarmoon.spark_core.api.animation.anim.auto_anim.EntityStateAutoAnim
import cn.solarmoon.spark_core.api.animation.anim.auto_anim.getAutoAnim
import cn.solarmoon.spark_core.api.animation.sync.SyncedAnimation
import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.entity.attack.clearAttackedData
import cn.solarmoon.spark_core.api.entity.preinput.getPreInput
import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.FightSkillController
import cn.solarmoon.spirit_of_fight.feature.fight_skill.sync.MovePayload
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d.div

open class CommonGuardAnimSkill(
    controller: FightSkillController,
    val animGroup: Map<AnimType, SyncedAnimation>,
    guardRange: Double,
): GuardAnimSkill(
    controller,
    buildSet { animGroup.values.forEach { add(it.anim.name) } },
    guardRange
) {

    enum class AnimType { IDLE, HURT }

    companion object {
        @JvmStatic
        fun createGuardSyncedAnim(prefix: String): Map<AnimType, SyncedAnimation> = buildMap {
            AnimType.entries.forEach {
                val name = it.toString().lowercase()
                when(it) {
                    AnimType.IDLE -> put(it, SyncedAnimation(MixedAnimation("$prefix:guard_${name}", startTransSpeed = 5f)))
                    AnimType.HURT -> put(it, SyncedAnimation(MixedAnimation("$prefix:guard_${name}", startTransSpeed = 10f)))
                }
            }
        }
    }

    val idleAnim = animGroup[AnimType.IDLE]!!
    val hurtAnim = animGroup[AnimType.HURT]!!

    fun isStanding(filter: (MixedAnimation) -> Boolean = {true}) = isPlaying { it.name == idleAnim.anim.name && filter.invoke(it) }

    fun isBacking(filter: (MixedAnimation) -> Boolean = {true}) = isPlaying { it.name == hurtAnim.anim.name && filter.invoke(it) }

    override fun start(sync: (SyncedAnimation) -> Unit) {
        entity.getPreInput().setInput("guard") {
            idleAnim.consume(animatable)
            sync.invoke(idleAnim)
        }
    }

    override fun stop(sync: (SyncedAnimation) -> Unit) {
        if (entity.getPreInput().id == "guard") entity.getPreInput().clear()
        super.stop(sync)
    }

    override fun shouldSummonBox(anim: MixedAnimation): Boolean {
        return !anim.isInTransition
    }

    override fun whenInAnim(anim: MixedAnimation) {
        super.whenInAnim(anim)

        // 击退动作的末尾续上站立动画
        if (anim.name == hurtAnim.anim.name) {
            if (anim.tick >= anim.maxTick && !isStanding()) {
                animatable.animController.stopAndAddAnimation(idleAnim.anim.apply { startTransSpeed = 4f })
            }
        }

        // 格挡行走时混合一个走路动画
        if (!anim.isCancelled && anim.name == idleAnim.anim.name) {
            animatable.getAutoAnim<EntityStateAutoAnim>("EntityState")?.blendWithoutArms(false) { it.name !in animBounds }
        }
    }

    override fun getBoxId(index: Int): String {
        return "${entity.id}:guard"
    }

    override fun onSuccessGuard(attackerPos: Vec3, damageSource: DamageSource, value: Float, anim: MixedAnimation): Boolean {
        if (getPlayingAnim{ !it.isCancelled }?.name == idleAnim.anim.name) {
            if (entity is LivingEntity) {
                hurtAnim.consume(animatable)
                hurtAnim.syncToClient(entity.id)
                val v = Vec3(entity.x - attackerPos.x, entity.y - attackerPos.y, entity.z - attackerPos.z).normalize().div(2.5)
                MovePayload.moveEntityInClient(entity.id, v)
            }
        }
        entity.clearAttackedData()
        return false
    }

}