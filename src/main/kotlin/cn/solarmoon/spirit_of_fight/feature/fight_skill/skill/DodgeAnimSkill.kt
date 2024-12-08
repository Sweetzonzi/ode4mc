package cn.solarmoon.spirit_of_fight.feature.fight_skill.skill

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.animation.sync.SyncedAnimation
import cn.solarmoon.spark_core.api.entity.preinput.getPreInput
import cn.solarmoon.spark_core.api.entity.skill.AnimSkill
import cn.solarmoon.spark_core.api.util.MoveDirection
import cn.solarmoon.spark_core.registry.common.SparkVisualEffects
import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.FightSkillController
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.phys.Vec3

open class DodgeAnimSkill(
    private val controller: FightSkillController,
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
                put(it, SyncedAnimation(MixedAnimation("$prefix:dodge_$it", startTransSpeed = 10f)))
            }
        }
    }

    var dodgeMoveVector = Vec3.ZERO

    fun isMatchPrecision(anim: MixedAnimation) = !anim.isInTransition && anim.isTickIn(0.0, 0.05)

    fun getMoveVector(mul: Double = 1.0): Vec3 = Vec3(dodgeMoveVector.x * moveSpeed * mul, entity.deltaMovement.y, dodgeMoveVector.z * moveSpeed * mul)

    fun start(direction: MoveDirection, vector: Vec3, sync: (SyncedAnimation) -> Unit = {}) {
        entity.getPreInput().setInput("dodge") {
            dodgeMoveVector = vector
            val anim = animGroup[direction]!!
            anim.consume(animatable)
            sync.invoke(anim)
        }
    }

    override fun getMove(anim: MixedAnimation): Vec3? {
        if (anim.isTickIn(0.0, dodgeTime)) {
            return getMoveVector(1 - anim.tick / ((dodgeTime + 0.05) * 20))
        }
        return null
    }

    override fun whenAttackedInAnim(damageSource: DamageSource, value: Float, anim: MixedAnimation): Boolean {
        if (isMatchPrecision(anim)) {
            onPrecisionDodge(damageSource, value, anim)
        }

        return anim.isInTransition || !anim.isTickIn(0.0, 0.42)
    }

    override fun whenNotInAnim() {
        super.whenNotInAnim()
    }

    open fun onPrecisionDodge(damageSource: DamageSource, value: Float, anim: MixedAnimation) {
        if (!entity.level().isClientSide) {
            SparkVisualEffects.SHADOW.addToClient(entity.id)
        }
    }

    override fun whenInAnim(anim: MixedAnimation) {
        super.whenInAnim(anim)
        // 在可切换节点可以直接切换除了闪避以外的技能（因为得保证闪避必有非无敌的后摇）
        if (anim.isTickIn(switchTime, Double.MAX_VALUE)) {
            entity.getPreInput().executeExcept("dodge")
        }
    }

    override fun physTick() {
        super.physTick()
        // 在正常防守时可以直接按出闪避
        if (controller.guard.isStanding()) {
            entity.getPreInput().executeIfPresent("dodge")
        }
    }

}