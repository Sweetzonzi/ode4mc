package test

import cn.solarmoon.spark_core.api.animation.BossRenderer
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.client.event.EntityRenderersEvent

object cep {

    private fun reg(event: EntityRenderersEvent.RegisterRenderers) {
        event.registerEntityRenderer(ees.BOSS.get(), ::BossRenderer)
    }

    @JvmStatic
    fun register(bus: IEventBus) {
        bus.addListener(::reg)
    }

}