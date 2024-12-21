package cn.solarmoon.spark_core.registry.client

import cn.solarmoon.spark_core.api.phys.DxBoundingBoxEntity
import cn.solarmoon.spark_core.api.phys.DxEntityRenderer
import cn.solarmoon.spark_core.registry.common.SparkEntityTypes
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.client.event.EntityRenderersEvent

object SparkEntityRendererRegister {

    private fun reg(event: EntityRenderersEvent.RegisterRenderers) {
        event.registerEntityRenderer(SparkEntityTypes.DX_BOUNDING_BOX.get(), DxEntityRenderer<DxBoundingBoxEntity>::create)
        event.registerEntityRenderer(SparkEntityTypes.DX_ANIM_ATTACK.get(), DxEntityRenderer<DxBoundingBoxEntity>::create)
    }

    @JvmStatic
    fun register(bus: IEventBus) {
        bus.addListener(::reg)
    }

}