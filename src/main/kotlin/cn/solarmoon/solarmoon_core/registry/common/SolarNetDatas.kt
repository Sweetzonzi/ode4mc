package cn.solarmoon.solarmoon_core.registry.common

import cn.solarmoon.solarmoon_core.api.network.CommonNetData
import net.neoforged.bus.api.IEventBus


object SolarNetDatas {

    @JvmStatic
    fun register(modBus: IEventBus) {
        modBus.addListener(CommonNetData::register)
    }

}