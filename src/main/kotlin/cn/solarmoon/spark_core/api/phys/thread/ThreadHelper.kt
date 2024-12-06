package cn.solarmoon.spark_core.api.phys.thread

import cn.solarmoon.spark_core.registry.common.SparkAttachments
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import kotlin.jvm.optionals.getOrNull

fun Level.getPhysLevel() = getData(SparkAttachments.PHYS_LEVEL).getOrNull()

fun Entity.getPhysLevel() = getData(SparkAttachments.PHYS_LEVEL).getOrNull()

fun Entity.getPhysSyncedData() = getData(SparkAttachments.PHYS_SYNCED_DATA)