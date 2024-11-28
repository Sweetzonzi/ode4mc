package cn.solarmoon.spirit_of_fight.feature.fight_skill.skill

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.sync.SyncedAnimation
import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.util.MoveDirection
import cn.solarmoon.spark_core.api.entity.skill.AnimSkill
import cn.solarmoon.spark_core.api.entity.preinput.getPreInput
import cn.solarmoon.spark_core.registry.client.SparkVisualEffectRenderers
import cn.solarmoon.spirit_of_fight.feature.hit.HitType
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.phys.Vec3
import thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d.times

open class DodgeAnimSkill(
    animatable: IEntityAnimatable<*>,
    val animGroup: Map<MoveDirection, SyncedAnimation>,
    private val switchTime: Double,
    private val dodgeTime: Double = 0.4,
    private val moveSpeed: Float = 0.7f
): AnimSkill(
    animatable,
    buildSet { animGroup.forEach { add(it.value.anim.name) } }
) {

    companion object {
        @JvmStatic
        fun createDodgeConsumeAnims(prefix: String): Map<MoveDirection, SyncedAnimation> = buildMap {
            MoveDirection.entries.forEach {
                put(it, SyncedAnimation(MixedAnimation("$prefix:dodge_$it", startTransSpeed = 5f)))
            }
        }
    }

    var dodgeMoveVector = Vec3.ZERO

    val preInput get() = entity.getPreInput()

    fun isMatchPrecision(anim: MixedAnimation) = !anim.isInTransition && anim.isTickIn(0.0, 0.05)

    fun getMoveVector(): Vec3 = Vec3(dodgeMoveVector.x * moveSpeed, entity.deltaMovement.y, dodgeMoveVector.z * moveSpeed)

    fun start(direction: MoveDirection, vector: Vec3, sync: (SyncedAnimation) -> Unit = {}) {
        preInput.setInput("dodge") {
            dodgeMoveVector = vector
            val anim = animGroup[direction]!!
            anim.consume(animatable)
            sync.invoke(anim)
        }
    }

    override fun getMove(anim: MixedAnimation): Vec3? {
        if (anim.isTickIn(0.0, dodgeTime)) {
            return getMoveVector().times(1 - anim.tick / ((dodgeTime + 0.05) * 20))
        }
        return null
    }

    override fun whenAttackedInAnim(damageSource: DamageSource, value: Float, anim: MixedAnimation): Boolean {
        if (isMatchPrecision(anim)) {
            onPrecisionDodge(damageSource, value, anim)
        }

        return anim.isInTransition || !anim.isTickIn(0.0, 0.42)
    }

    open fun onPrecisionDodge(damageSource: DamageSource, value: Float, anim: MixedAnimation) {
        if (!entity.level().isClientSide) {
            SparkVisualEffectRenderers.SHADOW.addToClient(entity.id)
        }
    }

    override fun whenInAnim(anim: MixedAnimation) {
        super.whenInAnim(anim)

        if (preInput.hasInput() && preInput.id != "dodge" && !HitType.isPlayingHitAnim(animatable) { !it.isCancelled } ) {
            if (anim.isTickIn(switchTime, Double.MAX_VALUE)) {
                preInput.invokeInput()
            }
        }
    }

}