package cn.solarmoon.spark_core.api.phys.thread

import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.neoforged.neoforge.common.NeoForge

class ClientPhysLevel(
    override val level: ClientLevel
): PhysLevel(level) {

    override fun frequencyTick() {
        val player = Minecraft.getInstance().player ?: return
        level.getEntities(null, player.boundingBox.inflate(100.0)).forEach {
            NeoForge.EVENT_BUS.post(PhysLevelTickEvent.Entity(it))
        }
    }

}