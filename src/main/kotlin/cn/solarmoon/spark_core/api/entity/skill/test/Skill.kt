package cn.solarmoon.spark_core.api.entity.skill.test

import cn.solarmoon.spark_core.registry.common.SparkRegistries

interface Skill {

    val registryKey get() = SparkRegistries.SKILL.getKey(this) ?: throw NullPointerException("Skill ${this.javaClass::getSimpleName} not yet registered.")

    fun activate(ob: Any)

    fun tick(ob: Any)

    //fun isPlaying(ob: Any): Boolean

}