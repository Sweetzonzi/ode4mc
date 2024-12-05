package cn.solarmoon.spirit_of_fight.feature.fight_skill.attack

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.anim.auto_anim.isPlayingHitAnim
import cn.solarmoon.spirit_of_fight.feature.fight_skill.IFightSkillHolder
import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.CommonFightSkillController
import cn.solarmoon.spirit_of_fight.feature.fight_skill.skill.ParryAnimSkill


fun IEntityAnimatable<*>.shouldOperateFreezing(): Boolean {
    val entity = animatable

    val isHitting = isPlayingHitAnim { !it.isCancelled }
    val isParried = ParryAnimSkill.PARRY_SYNCED_ANIM.any { it.value.isPlaying(this) { !it.isCancelled } }
    val isParring = (((entity as? IFightSkillHolder)?.skillController as? CommonFightSkillController)?.parry?.isPlaying { !it.isCancelled }) == true
    val isGuardBacking = entity is IFightSkillHolder && entity.skillController?.guard?.isBacking { !it.isInTransition } == true
    val isAttacking = entity is IFightSkillHolder && entity.skillController?.isAttacking { !it.isInTransition } == true

    return isHitting || isParried || isParring || isGuardBacking || isAttacking
}