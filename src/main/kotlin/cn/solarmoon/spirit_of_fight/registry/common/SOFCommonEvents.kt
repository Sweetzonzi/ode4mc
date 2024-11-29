package cn.solarmoon.spirit_of_fight.registry.common

import cn.solarmoon.spirit_of_fight.feature.fight_skill.attack.AttackModifier
import cn.solarmoon.spirit_of_fight.feature.fight_skill.spirit.FightSpiritApplier
import cn.solarmoon.spirit_of_fight.feature.hit.HitAnimationApplier
import net.neoforged.neoforge.common.NeoForge

object SOFCommonEvents {

    @JvmStatic
    fun register() {
        add(AttackModifier())
        add(HitAnimationApplier())
        add(FightSpiritApplier())
    }

    private fun add(event: Any) {
        NeoForge.EVENT_BUS.register(event)
    }

}