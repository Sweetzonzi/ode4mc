package cn.solarmoon.spark_core.registry.client

import cn.solarmoon.spark_core.api.util.TooltipGatherUtil
import cn.solarmoon.spark_core.feature.inlay.InlayTooltip
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent
import net.neoforged.neoforge.client.event.RenderTooltipEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.common.NeoForgeMod

object SparkTooltips {

    @JvmStatic
    private fun addTooltips(event: RegisterClientTooltipComponentFactoriesEvent) {
        event.register(InlayTooltip.Component::class.java, ::InlayTooltip)
    }

    @JvmStatic
    @SubscribeEvent
    private fun gatherTooltips(event: RenderTooltipEvent.GatherComponents) {
        TooltipGatherUtil.gatherToFirstEmpty(event, InlayTooltip.Component(event.itemStack))
    }

    @JvmStatic
    fun register(bus: IEventBus) {
        NeoForge.EVENT_BUS.register(SparkTooltips::class.java)
        bus.addListener(this::addTooltips)
    }

}