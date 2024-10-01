package cn.solarmoon.spark_core.registry.common

import cn.solarmoon.spark_core.api.animation.sync.AnimDataRefraction
import cn.solarmoon.spark_core.api.animation.sync.AnimNetData
import cn.solarmoon.spark_core.api.animation.sync.WholeModelData
import cn.solarmoon.spark_core.api.animation.sync.WholeModelDataRefraction
import cn.solarmoon.spark_core.api.network.CommonNetData
import cn.solarmoon.spark_core.api.phys.collision.FreeCollisionBoxData
import cn.solarmoon.spark_core.api.phys.collision.FreeCollisionBoxSyncer
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler


object SparkNetDatas {

    private fun reg(event: RegisterPayloadHandlersEvent) {
        val registrar = event.registrar("animation")
        registrar.playToClient(AnimNetData.TYPE, AnimNetData.STREAM_CODEC, AnimDataRefraction())
        registrar.playToClient(WholeModelData.TYPE, WholeModelData.STREAM_CODEC, WholeModelDataRefraction())
        val r2 = event.registrar("box")
        r2.playBidirectional(FreeCollisionBoxData.TYPE, FreeCollisionBoxData.STREAM_CODEC, DirectionalPayloadHandler(FreeCollisionBoxSyncer.Client(), FreeCollisionBoxSyncer.Server()))
    }

    @JvmStatic
    fun register(modBus: IEventBus) {
        modBus.addListener(CommonNetData::register)
        modBus.addListener(::reg)
    }

}