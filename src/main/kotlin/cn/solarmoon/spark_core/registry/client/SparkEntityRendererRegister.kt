package cn.solarmoon.spark_core.registry.client

import cn.solarmoon.spark_core.registry.common.SparkEntityTypes
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.client.event.EntityRenderersEvent

object SparkEntityRendererRegister {

    private fun reg(event: EntityRenderersEvent.RegisterRenderers) {

    }

    @JvmStatic
    fun register(bus: IEventBus) {
        bus.addListener(::reg)
    }

}