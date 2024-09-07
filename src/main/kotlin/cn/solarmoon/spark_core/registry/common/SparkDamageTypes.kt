package cn.solarmoon.spark_core.registry.common

import cn.solarmoon.spark_core.SparkCore

object SparkDamageTypes {

    @JvmStatic
    fun register() {}

    @JvmStatic
    val THORNS = SparkCore.REGISTER.damageType()
        .id("thorns")
        .build()

}