package cn.solarmoon.spirit_of_fight.registry.client

import cn.solarmoon.spark_core.event.LocalControllerRegisterEvent
import cn.solarmoon.spirit_of_fight.fighter.player.PlayerLocalController
import net.neoforged.bus.api.IEventBus

object SOFLocalControllerRegister {

    private fun reg(event: LocalControllerRegisterEvent) {
        event.register(PlayerLocalController)
    }

    @JvmStatic
    fun register(bus: IEventBus) {
        bus.addListener(::reg)
    }

}