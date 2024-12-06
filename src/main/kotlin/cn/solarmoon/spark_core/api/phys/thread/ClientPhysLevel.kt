package cn.solarmoon.spark_core.api.phys.thread

import cn.solarmoon.spark_core.SparkCore
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.neoforged.neoforge.common.NeoForge

class ClientPhysLevel(
    override val level: ClientLevel
): PhysLevel(level) {

    val partialTicks: Float get() {
        val currentTime = System.nanoTime()
        val deltaTime = (currentTime - lastTickTime) / 1_000_000.0
        return (deltaTime / TICK_STEP).toFloat().coerceIn(0f, 1f)
    }

    override fun frequencyTick() {
        val player = Minecraft.getInstance().player ?: return
        level.getEntities(null, player.boundingBox.inflate(100.0)).forEach {
            NeoForge.EVENT_BUS.post(PhysLevelTickEvent.Entity(this, it))
        }
        NeoForge.EVENT_BUS.post(PhysLevelTickEvent.FrequencyInput(this))
    }

}