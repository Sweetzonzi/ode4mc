package cn.solarmoon.spark_core.registry.client

import net.neoforged.neoforge.common.NeoForge

object SparkClientEvents {

    @JvmStatic
    fun register() {

    }

    private fun add(event: Any) {
        NeoForge.EVENT_BUS.register(event)
    }

}