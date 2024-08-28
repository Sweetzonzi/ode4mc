package cn.solarmoon.solarmoon_core.api.kit

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

/**
 * 翻译键实用程序
 */
class Translator(private val modId: String) {

    fun set(string1: String, string2: String, vararg objects: Any): Component {
        return Component.translatable("$string1.$modId.$string2", *objects)
    }

    fun set(string1: String, string2: String, format: ChatFormatting, vararg objects: Any): Component {
        return Component.translatable("$string1.$modId.$string2", *objects)
            .withStyle(format)
    }

}