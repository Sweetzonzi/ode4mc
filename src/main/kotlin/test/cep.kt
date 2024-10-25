package test

import cn.solarmoon.spark_core.api.animation.renderer.GeoLivingEntityRenderer
import cn.solarmoon.spark_core.api.animation.renderer.layer.GlowingTextureLayer
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import test.runestone_dungeon_keeper.Tiasis

object cep {

    private fun reg(event: EntityRenderersEvent.RegisterRenderers) {
        event.registerEntityRenderer(EES.BOSS2.get()) {
            val e: GeoLivingEntityRenderer<Tiasis> = GeoLivingEntityRenderer(it, 0.5f)
            e.layers.add(GlowingTextureLayer(e))
            e
        }
        event.registerEntityRenderer(EES.BOSS.get()) { GeoLivingEntityRenderer(it, 0.5f) }
        event.registerEntityRenderer(EES.BOSS3.get()) { GeoLivingEntityRenderer(it, 0.5f) }
        event.registerEntityRenderer(EES.BOSS4.get()) { GeoLivingEntityRenderer(it, 0.5f) }
        event.registerEntityRenderer(EES.BOSS5.get()) { GeoLivingEntityRenderer(it, 0.5f) }
        event.registerEntityRenderer(EES.BOSS6.get()) { GeoLivingEntityRenderer(it, 0.5f) }
        event.registerEntityRenderer(EES.BOSS7.get()) { GeoLivingEntityRenderer(it, 0.5f) }
        event.registerEntityRenderer(EES.BOSS8.get()) { GeoLivingEntityRenderer(it, 0.5f) }
    }

    @JvmStatic
    fun register(bus: IEventBus) {
        bus.addListener(::reg)
    }

}