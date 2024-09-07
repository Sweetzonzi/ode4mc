package cn.solarmoon.spark_core.api.attachment.counting

import cn.solarmoon.spark_core.registry.common.SparkAttachments
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.PlayerTickEvent

class CountingDeviceTick {

    @SubscribeEvent
    fun onPlayerTick(event: PlayerTickEvent.Pre) {
        val device = event.entity.getData(SparkAttachments.COUNTING_DEVICE)
        device.tick()
    }

}