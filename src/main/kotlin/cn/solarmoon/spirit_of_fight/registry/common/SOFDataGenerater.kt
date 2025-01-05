package cn.solarmoon.spirit_of_fight.registry.common

import cn.solarmoon.spirit_of_fight.data.SOFSkillTags
import net.minecraft.data.DataProvider
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.data.event.GatherDataEvent


object SOFDataGenerater {

    @SubscribeEvent
    private fun gather(event: GatherDataEvent) {
        val generator = event.generator
        val output = generator.packOutput
        val lookupProvider = event.lookupProvider
        val helper = event.existingFileHelper

        fun addProvider(provider: DataProvider) = generator.addProvider(event.includeServer(), provider)

        addProvider(SOFSkillTags(output, lookupProvider, helper))
    }

    @JvmStatic
    fun register(bus: IEventBus) {
        bus.addListener(::gather)
    }

}