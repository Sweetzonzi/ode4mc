package cn.solarmoon.spark_core.registry.common

import cn.solarmoon.spark_core.SparkCore

object SparkAttributes {

    @JvmStatic
    fun register() {}

    @JvmStatic
    val THORNS = SparkCore.REGISTER.attribute()
        .id("thorns")
        .boundRanged(0.0, 0.0, 1024.0)
        .applyToLivingEntity()
        .build()

}