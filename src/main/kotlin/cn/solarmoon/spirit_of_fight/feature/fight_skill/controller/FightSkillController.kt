package cn.solarmoon.spirit_of_fight.feature.fight_skill.controller

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.entity.skill.AnimSkill
import cn.solarmoon.spark_core.api.entity.skill.AnimSkillController
import cn.solarmoon.spark_core.api.entity.preinput.getPreInput
import cn.solarmoon.spirit_of_fight.feature.fight_skill.skill.ComboAnimSkill
import cn.solarmoon.spirit_of_fight.feature.fight_skill.skill.DodgeAnimSkill
import cn.solarmoon.spirit_of_fight.feature.fight_skill.skill.GuardAnimSkill
import cn.solarmoon.spirit_of_fight.feature.fight_skill.skill.JumpAttackAnimSkill
import cn.solarmoon.spirit_of_fight.feature.fight_skill.skill.SingleAttackAnimSkill
import cn.solarmoon.spirit_of_fight.feature.fight_skill.skill.SprintAttackAnimSkill
import cn.solarmoon.spirit_of_fight.feature.hit.HitType
import org.joml.Vector3f

/**
 * 武器连招系统
 */
abstract class FightSkillController(
    val animatable: IEntityAnimatable<*>,
    val id: String,
    val baseAttackSpeed: Float,
    val commonBoxSize: Vector3f,
    val commonBoxOffset: Vector3f = Vector3f(0f, 0f, commonBoxSize.z / -2)
): AnimSkillController() {

    val entity get() = animatable.animatable
    val preInput get() = entity.getPreInput()

    abstract val combo: ComboAnimSkill
    abstract val dodge: DodgeAnimSkill
    abstract val guard: GuardAnimSkill
    abstract val jumpAttack: JumpAttackAnimSkill
    abstract val sprintAttack: SprintAttackAnimSkill

    open val specialAttackSkillGroup: MutableList<SingleAttackAnimSkill> get() = mutableListOf(
        jumpAttack, sprintAttack
    )

    override val skillGroup: List<AnimSkill>
        get() = mutableListOf(combo, dodge, guard).apply { addAll(specialAttackSkillGroup) }

    override fun isAttacking(filter: (MixedAnimation) -> Boolean): Boolean {
        return combo.isPlaying(filter) || specialAttackSkillGroup.any { it.isPlaying(filter) }
    }

    override fun tick() {
        super.tick()
        // 不在播放任何动画，直接进行预输入释放
        if (!isPlayingSkill { !it.isCancelled } || (preInput.hasInput("dodge") && guard.isStanding())) { // 格挡时可以不预输入直接闪避
            // 但是都不能在受击硬直时输入
            if (!HitType.isPlayingHitAnim(animatable) { !it.isCancelled }) {
                combo.index = 0
                if (preInput.hasInput()) preInput.invokeInput()
            }
        }
    }

    override fun onDisabledMoment() {
        super.onDisabledMoment()
        if (preInput.hasInput()) preInput.clear()
    }

}