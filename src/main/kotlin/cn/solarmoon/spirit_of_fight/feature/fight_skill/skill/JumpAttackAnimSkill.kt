package cn.solarmoon.spirit_of_fight.feature.fight_skill.skill

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.sync.SyncedAnimation
import cn.solarmoon.spark_core.api.entity.state.isFalling
import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.FightSkillController
import cn.solarmoon.spirit_of_fight.feature.hit.HitType
import org.joml.Vector3f

abstract class JumpAttackAnimSkill(
    controller: FightSkillController,
    attackAnim: SyncedAnimation,
    damageMultiplier: Float,
    switchTime: Double,
    hitType: HitType
): SingleAttackAnimSkill(controller, attackAnim, damageMultiplier, switchTime, hitType) {

    override val isMetCondition: Boolean
        get() = entity.isFalling() && !isPlaying() // 跳跃不能预输入，因为必须保证只能在空中释放

}