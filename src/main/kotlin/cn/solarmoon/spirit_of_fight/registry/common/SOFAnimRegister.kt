package cn.solarmoon.spirit_of_fight.registry.common

import cn.solarmoon.spark_core.animation.anim.auto_anim.AutoAnimRegisterEvent
import cn.solarmoon.spirit_of_fight.feature.hit.HumanoidWeaponHitAutoAnim
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.NeoForge

object SOFAnimRegister {

    @SubscribeEvent
    private fun reg(event: AutoAnimRegisterEvent.Entity) {
        event.register { a, e -> HumanoidWeaponHitAutoAnim(a, e) }
    }

    @JvmStatic
    fun register() {
        NeoForge.EVENT_BUS.register(this)
    }

}