package cn.solarmoon.spirit_of_fight.feature.fight_skill.controller

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.entity.skill.AnimSkill
import cn.solarmoon.spirit_of_fight.feature.fight_skill.skill.ParryAnimSkill
import org.joml.Vector3f

abstract class CommonFightSkillController(
    animatable: IEntityAnimatable<*>,
    id: String,
    baseAttackSpeed: Float,
    commonBoxSize: Vector3f,
    commonBoxOffset: Vector3f = Vector3f(0f, 0f, commonBoxSize.z / -2)
): FightSkillController(animatable, id, baseAttackSpeed, commonBoxSize, commonBoxOffset) {

    abstract val parry: ParryAnimSkill

    override val skillGroup: List<AnimSkill>
        get() = super.skillGroup.toMutableList().apply { add(parry) }

}