package cn.solarmoon.solarmoon_core.api.attachment.counting

import cn.solarmoon.solarmoon_core.SolarMoonCore
import cn.solarmoon.solarmoon_core.api.data.element.FoodValue
import cn.solarmoon.solarmoon_core.registry.common.CommonAttachments
import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import kotlinx.serialization.json.JsonElement
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.PlayerTickEvent

class CountingDeviceTick {

    @SubscribeEvent
    fun onPlayerTick(event: PlayerTickEvent.Pre) {
        val device = event.entity.getData(CommonAttachments.COUNTING_DEVICE)
        device.tick()
    }

}