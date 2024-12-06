package cn.solarmoon.spark_core.api.phys.thread

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.phys.math.DVector3
import cn.solarmoon.spark_core.api.phys.ode.DContactBuffer
import cn.solarmoon.spark_core.api.phys.ode.OdeHelper
import cn.solarmoon.spark_core.api.phys.toDVector3
import cn.solarmoon.spark_core.registry.common.SparkAttachments
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent
import net.neoforged.neoforge.event.level.LevelEvent
import net.neoforged.neoforge.event.tick.EntityTickEvent
import java.util.Optional

class PhysThreadApplier {

    @SubscribeEvent
    private fun onLevelLoad(event: LevelEvent.Load) {
        val level = event.level

        if (level.isClientSide) {
            val pl = ClientPhysLevel(level as ClientLevel)
            level.setPhysLevel(pl)
            pl.load()
        } else {
            val pl = ServerPhysLevel(level as ServerLevel)
            level.setPhysLevel(pl)
            pl.load()
        }
    }

    @SubscribeEvent
    private fun onLevelUnload(event: LevelEvent.Unload) {
        val level = event.level

        if (level.isClientSide) {
            val pl = ClientPhysLevel(level as ClientLevel)
            level.setPhysLevel(pl)
            pl.unLoad()
        } else {
            val pl = ServerPhysLevel(level as ServerLevel)
            level.setPhysLevel(pl)
            pl.unLoad()
        }
    }

    @SubscribeEvent
    private fun addEntity(event: EntityJoinLevelEvent) {
        event.level.getPhysLevel().let {
            event.entity.setPhysLevel(it)
        }
    }

    @SubscribeEvent
    private fun test(event: PhysLevelTickEvent.Entity) {
        val entity = event.entity
        entity.getPhysLevel()?.let {
            val entityBox = OdeHelper.createBox(5.0, 5.0, 5.0).apply { position = entity.position().toDVector3() }
            val box = OdeHelper.createBox(DVector3(5.0, 5.0, 5.0)).apply { position = Vec3.ZERO.toDVector3(); }
            if (OdeHelper.collide(entityBox, box, 1, DContactBuffer(64).geomBuffer) > 0) {

            }
        }
    }

    @SubscribeEvent
    private fun eTick(event: EntityTickEvent.Pre) {
        val entity = event.entity
    }

}