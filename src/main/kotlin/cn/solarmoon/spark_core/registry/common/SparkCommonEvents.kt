package cn.solarmoon.spark_core.registry.common

import cn.solarmoon.spark_core.api.animation.anim.play.AnimTicker
import cn.solarmoon.spark_core.api.entity.attack.AttackedDataController
import cn.solarmoon.spark_core.api.entity.preinput.PreInputApplier
import cn.solarmoon.spark_core.api.entity.skill.AnimSkillApplier
import cn.solarmoon.spark_core.api.entity.state.EntityStateModifier
import net.neoforged.neoforge.common.NeoForge

object SparkCommonEvents {

    @JvmStatic
    fun register() {
        add(AnimTicker())
        add(AttackedDataController())
        add(AnimSkillApplier())
        add(EntityStateModifier())
        add(PreInputApplier())
    }

    private fun add(event: Any) {
        NeoForge.EVENT_BUS.register(event)
    }

}