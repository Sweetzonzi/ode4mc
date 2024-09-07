package cn.solarmoon.spark_core.registry.common

import cn.solarmoon.spark_core.api.command.CommandRegisterHelper
import cn.solarmoon.spark_core.element.command.GetTagsCommand
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.RegisterCommandsEvent

object SparkCommands {

    @SubscribeEvent
    private fun registries(event: RegisterCommandsEvent) {
        val helper = CommandRegisterHelper(event)
        helper.add(GetTagsCommand("getTags", 2, true))
    }

    @JvmStatic
    fun register() {
        NeoForge.EVENT_BUS.register(SparkCommands)
    }

}