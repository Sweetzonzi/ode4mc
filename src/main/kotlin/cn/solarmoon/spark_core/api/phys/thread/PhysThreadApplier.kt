package cn.solarmoon.spark_core.api.phys.thread

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.server.level.ServerLevel
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent
import net.neoforged.neoforge.event.level.LevelEvent
import net.neoforged.neoforge.event.tick.EntityTickEvent
import net.neoforged.neoforge.event.tick.LevelTickEvent

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
    private fun levelActionConsumer(event: LevelTickEvent.Post) {
        val level = event.level
        val actions = (level as IActionConsumer).getActions()
        actions.forEach { it.invoke() }
        actions.clear()
    }

}