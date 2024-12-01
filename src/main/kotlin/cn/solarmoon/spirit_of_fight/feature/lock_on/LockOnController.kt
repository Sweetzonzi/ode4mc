package cn.solarmoon.spirit_of_fight.feature.lock_on

import cn.solarmoon.spark_core.api.entity.state.smoothLookAt
import cn.solarmoon.spark_core.api.event.EntityTurnEvent
import cn.solarmoon.spirit_of_fight.registry.client.SOFKeyMappings
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.Vec3
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.client.event.InputEvent
import net.neoforged.neoforge.client.event.ViewportEvent

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