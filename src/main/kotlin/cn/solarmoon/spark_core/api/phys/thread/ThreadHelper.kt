package cn.solarmoon.spark_core.api.phys.thread

import cn.solarmoon.spark_core.registry.common.SparkAttachments
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.neoforged.neoforge.attachment.IAttachmentHolder
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

fun IAttachmentHolder.getPhysLevel() = getData(SparkAttachments.PHYS_LEVEL).getOrNull()

fun IAttachmentHolder.setPhysLevel(physLevel: PhysLevel?) = setData(SparkAttachments.PHYS_LEVEL, Optional.ofNullable(physLevel))

fun Entity.getPhysSyncedData() = getData(SparkAttachments.PHYS_SYNCED_DATA)