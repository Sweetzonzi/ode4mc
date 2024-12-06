package cn.solarmoon.spirit_of_fight.feature.lock_on

import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

object LockOnController {

    private var TARGET: Entity? = null

    @JvmStatic
    val target get() = TARGET

    @JvmStatic
    val hasTarget get() = TARGET != null

    @JvmStatic
    val lookPos get() = target?.boundingBox?.center ?: Vec3.ZERO

    @JvmStatic
    fun setTarget(entity: Entity?) {
        TARGET = entity
    }

    @JvmStatic
    fun clear() {
        setTarget(null)
    }

}