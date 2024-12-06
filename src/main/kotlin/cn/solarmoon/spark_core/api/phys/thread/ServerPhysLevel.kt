package cn.solarmoon.spark_core.api.phys.thread

import net.minecraft.server.level.ServerLevel
import net.neoforged.neoforge.common.NeoForge

class ServerPhysLevel(
    override val level: ServerLevel
): PhysLevel(level) {

    override fun frequencyTick() {
        level.allEntities.forEach {
            NeoForge.EVENT_BUS.post(PhysLevelTickEvent.Entity(this, it))
        }
    }

}