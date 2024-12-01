package cn.solarmoon.spirit_of_fight.feature.fight_skill.skill

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.sync.SyncedAnimation
import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.FightSkillController
import cn.solarmoon.spirit_of_fight.feature.hit.HitType
import org.joml.Vector3f

abstract class SprintAttackAnimSkill(
    controller: FightSkillController,
    attackAnim: SyncedAnimation,
    damageMultiplier: Float,
    switchTime: Double,
    hitType: HitType,
    hitStrength: Int
): SingleAttackAnimSkill(controller, attackAnim, damageMultiplier, switchTime, hitType, hitStrength) {

    override val isMetCondition: Boolean
        get() = entity.isSprinting

}