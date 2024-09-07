package cn.solarmoon.spark_core.registry.common

import cn.solarmoon.spark_core.api.network.CommonNetData
import net.neoforged.bus.api.IEventBus


object SparkNetDatas {

    @JvmStatic
    fun register(modBus: IEventBus) {
        modBus.addListener(CommonNetData::register)
    }

}