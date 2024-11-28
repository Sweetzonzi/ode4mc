package cn.solarmoon.spirit_of_fight.registry.client

import cn.solarmoon.spirit_of_fight.feature.fight_skill.attack.CameraLocker
import cn.solarmoon.spirit_of_fight.feature.fight_skill.attack.AttackController
import cn.solarmoon.spirit_of_fight.feature.fight_skill.spirit.FightSpiritGui
import net.neoforged.neoforge.common.NeoForge

object SOFClientEvents {

    @JvmStatic
    fun register() {
        add(CameraLocker())
        add(AttackController())
    }

    private fun add(event: Any) {
        NeoForge.EVENT_BUS.register(event)
    }

}