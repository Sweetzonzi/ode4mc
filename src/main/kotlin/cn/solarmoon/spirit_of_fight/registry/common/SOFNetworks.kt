package cn.solarmoon.spirit_of_fight.registry.common

import cn.solarmoon.spirit_of_fight.feature.fight_skill.sync.ClientOperationPayload
import cn.solarmoon.spirit_of_fight.feature.fight_skill.sync.FightSpiritPayload
import cn.solarmoon.spirit_of_fight.feature.fight_skill.sync.MovePayload
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent

object SOFNetworks {

    private fun net(event: RegisterPayloadHandlersEvent) {
        val control = event.registrar("client_control")
        control.playToServer(ClientOperationPayload.TYPE, ClientOperationPayload.STREAM_CODEC, ClientOperationPayload::handleInServer)
        control.playToClient(MovePayload.TYPE, MovePayload.STREAM_CODEC, MovePayload::handleInClient)
        val sync = event.registrar("sync")
        sync.playToClient(FightSpiritPayload.TYPE, FightSpiritPayload.STREAM_CODEC, FightSpiritPayload::handleInClient)
    }

    @JvmStatic
    fun register(modBus: IEventBus) {
        modBus.addListener(::net)
    }

}