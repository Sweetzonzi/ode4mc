package cn.solarmoon.spark_core.element.command

import cn.solarmoon.spark_core.api.command.CustomCommand
import com.mojang.brigadier.Command
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import java.util.function.Consumer
import java.util.stream.Collectors


class GetTagsCommand(head: String, permissionLevel: Int, isEnabled: Boolean) : CustomCommand(head, permissionLevel, isEnabled) {

    override fun putExecution() {
        builder.executes { getTags(it.source.playerOrException) }
    }

    fun getTags(player: ServerPlayer): Int {
        val stack = player.mainHandItem
        val tags = stack.tags.collect(Collectors.toSet())
        val strings = mutableListOf<String>()
        tags.forEach(Consumer { strings.add(it.location().toString()) })
        player.sendSystemMessage(Component.literal(strings.toString()))
        return Command.SINGLE_SUCCESS
    }

}