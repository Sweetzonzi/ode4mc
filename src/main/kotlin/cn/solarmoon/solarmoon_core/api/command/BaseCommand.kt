package cn.solarmoon.solarmoon_core.api.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

/**
 * 基本的命令模版
 * @param head 头部命令行名称
 * @param permissionLevel 权限等级
 * @param enabled 是否启用
 */
abstract class BaseCommand(head: String, permissionLevel: Int, private var enabled: Boolean) {

    private var builder: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal(head).requires { it.hasPermission(permissionLevel) }

    init {
        putExecution()
    }

    /**
     * 用于放入后续指令
     */
    abstract fun putExecution()

    /**
     * @return 返回最终的指令程序
     */
    fun getBuilder(): LiteralArgumentBuilder<CommandSourceStack> {
        return builder
    }

    /**
     * 方便快速识别是否启用该命令
     */
    fun isEnabled(): Boolean {
        return this.enabled
    }

}