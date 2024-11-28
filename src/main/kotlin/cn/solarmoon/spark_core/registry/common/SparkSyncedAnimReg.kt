package cn.solarmoon.spark_core.registry.common

import cn.solarmoon.spark_core.api.animation.sync.SyncedAnimation

object SparkSyncedAnimReg {

    @JvmStatic
    fun register() {
        SyncedAnimation.registerAnim()
    }

}