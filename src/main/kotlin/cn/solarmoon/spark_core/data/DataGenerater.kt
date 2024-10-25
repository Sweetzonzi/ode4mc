package cn.solarmoon.spark_core.data

import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.data.event.GatherDataEvent


object DataGenerater {//

    @SubscribeEvent
    private fun gather(event: GatherDataEvent) {
        val generator = event.generator
        val output = generator.packOutput
        val lookupProvider = event.lookupProvider

        generator.addProvider(
            event.includeServer(),
            RecipeDataProvider(output, lookupProvider)
        )
    }

    @JvmStatic
    fun register(bus: IEventBus) {
        bus.addListener(::gather)
    }

}