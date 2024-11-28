package cn.solarmoon.spirit_of_fight.registry.common

import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.SwordFightSkillController
import cn.solarmoon.spirit_of_fight.feature.hit.HitType

object SOFSyncedAnimReg {

    @JvmStatic
    fun register() {
        SwordFightSkillController.registerAnim()
        HitType.registerAnim()
    }

}