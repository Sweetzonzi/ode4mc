package cn.solarmoon.spark_core.registry.common

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.entity.skill.test.Skill

object SparkRegistries {

    @JvmStatic
    val SKILL = SparkCore.REGISTER.registry<Skill>()
        .id("skill")
        .build { it.sync(true).create() }

    @JvmStatic
    fun register() {}

}