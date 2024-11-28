package cn.solarmoon.spirit_of_fight.registry.client

import cn.solarmoon.spark_core.SparkCore
import com.mojang.blaze3d.platform.InputConstants
import org.lwjgl.glfw.GLFW

object SOFKeyMappings {

    @JvmStatic
    fun register() {}

    @JvmStatic
    val GUARD = SparkCore.REGISTER.keyMapping()
        .id("guard")
        .bound(GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        .type(InputConstants.Type.MOUSE)
        .build()

    @JvmStatic
    val DODGE = SparkCore.REGISTER.keyMapping()
        .id("dodge")
        .bound(GLFW.GLFW_KEY_LEFT_ALT)
        .build()

    @JvmStatic
    val SPECIAL_ATTACK = SparkCore.REGISTER.keyMapping()
        .id("special_attack")
        .bound(GLFW.GLFW_KEY_X)
        .build()

}