package cn.solarmoon.spirit_of_fight.registry.common

import cn.solarmoon.spark_core.api.animation.anim.auto_anim.AutoAnimRegisterEvent
import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.SwordFightSkillController
import cn.solarmoon.spirit_of_fight.feature.fight_skill.skill.ParryAnimSkill
import cn.solarmoon.spirit_of_fight.feature.hit.HumanoidWeaponHitAutoAnim
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.NeoForge

object SOFAnimRegister {

    @SubscribeEvent
    private fun reg(event: AutoAnimRegisterEvent.Entity) {
        event.register { a, e -> HumanoidWeaponHitAutoAnim(a, e) }
    }

    private fun regSynced() {
        SwordFightSkillController.registerAnim()
        HumanoidWeaponHitAutoAnim.registerAnim()
        ParryAnimSkill.registerAnim()
    }

    @JvmStatic
    fun register() {
        regSynced()
        NeoForge.EVENT_BUS.register(this)
    }

}