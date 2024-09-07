package cn.solarmoon.spark_core.api.command

import net.neoforged.neoforge.event.RegisterCommandsEvent

class CommandRegisterHelper(private val event: RegisterCommandsEvent) {

    fun add(command: CustomCommand) {
        command.isEnabled.let { event.dispatcher.register(command.builder) }
    }

}