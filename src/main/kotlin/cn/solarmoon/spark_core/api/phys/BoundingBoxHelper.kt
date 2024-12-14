package cn.solarmoon.spark_core.api.phys

import cn.solarmoon.spark_core.registry.common.SparkAttachments
import net.neoforged.neoforge.attachment.IAttachmentHolder

fun IAttachmentHolder.getBoundingBones() = getData(SparkAttachments.BOUNDING_BONES)

fun IAttachmentHolder.getBoundingBone(name: String): IBoundingBone {
    return getBoundingBones()[name]!!
}