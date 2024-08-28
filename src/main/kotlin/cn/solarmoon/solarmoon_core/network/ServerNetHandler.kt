package cn.solarmoon.solarmoon_core.network

import cn.solarmoon.solarmoon_core.api.network.CommonNetData
import cn.solarmoon.solarmoon_core.registry.common.CommonAttachments
import net.neoforged.neoforge.network.handling.IPayloadContext
import test.be

object ServerNetHandler {

    @JvmStatic
    fun handle(data: CommonNetData, context: IPayloadContext) {
        when(data.message) {

        }
    }

}