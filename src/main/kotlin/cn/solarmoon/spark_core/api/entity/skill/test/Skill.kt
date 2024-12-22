package cn.solarmoon.spark_core.api.entity.skill.test

import cn.solarmoon.spark_core.registry.common.SparkRegistries
import net.minecraft.world.entity.Entity

interface Skill<T> {

    fun activate(ob: T)

    fun tick(ob: T)

    val registryKey get() = SparkRegistries.SKILL.getKey(this) ?: throw NullPointerException("Skill ${this.javaClass::getSimpleName} not yet registered.")

}