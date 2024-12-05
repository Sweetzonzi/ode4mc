package cn.solarmoon.spirit_of_fight.feature.fight_skill.skill

import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.entity.skill.BoneBoxAnimSkill
import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.FightSkillController
import org.joml.Vector3f

abstract class ItemBoxAnimSkill(
    protected val controller: FightSkillController,
    animBounds: Set<String>,
): BoneBoxAnimSkill(controller.animatable, animBounds) {

    override val boxSize: Vector3f = controller.commonBoxSize
    override val boxOffset: Vector3f = controller.commonBoxOffset
    override fun getBoxBoundBoneName(anim: MixedAnimation): String = "rightItem"

}