package cn.solarmoon.spark_core.api.entity.ai.skill

import net.minecraft.world.entity.Entity

abstract class Skill<T: Entity>(
    val entity: T,
    var maxCooldown: Int
) {

    var cooldown = 0

    val isReady get() = cooldown == 0

    abstract val condition: Boolean

    abstract fun tick()

    abstract fun onStart()

    fun tryStart(): Boolean {
        if (isReady && condition) {
            start()
            return true
        }
        return false
    }

    fun start() {
        resetCooldown()
        onStart()
    }

    fun resetCooldown() {
        cooldown = maxCooldown
    }

    open fun cooldownTick() {
        if (cooldown > 0) cooldown--
    }

}