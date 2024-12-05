package cn.solarmoon.spark_core.registry.common

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.anim.play.AnimData
import cn.solarmoon.spark_core.api.entity.attack.AttackedData
import cn.solarmoon.spark_core.api.entity.preinput.PreInput
import cn.solarmoon.spark_core.api.phys.obb.MountableOBB
import cn.solarmoon.spark_core.api.phys.obb.OrientedBoundingBox
import java.util.Optional


object SparkAttachments {
    @JvmStatic
    fun register() {}

    @JvmStatic
    val ANIM_DATA = SparkCore.REGISTER.attachment<AnimData>()
        .id("anim_data")
        .defaultValue { AnimData.EMPTY }
        .serializer { it.serialize(AnimData.CODEC) }
        .build()

    @JvmStatic
    val ATTACKED_DATA = SparkCore.REGISTER.attachment<Optional<AttackedData>>()
        .id("attacked_data")
        .defaultValue { Optional.empty() }
        .serializer { it.serialize(AttackedData.OPTIONAL_CODEC) { it.isPresent } }
        .build()

    @JvmStatic
    val MOUNTABLE_OBB = SparkCore.REGISTER.attachment<MutableMap<String, MountableOBB>>()
        .id("mountable_obb")
        .defaultValue { mutableMapOf<String, MountableOBB>() }
        .serializer { it.serialize(MountableOBB.MAP_CODEC) }
        .build()

    @JvmStatic
    val OBB_CACHE = SparkCore.REGISTER.attachment<MutableMap<String, OrientedBoundingBox>>()
        .id("obb_cache")
        .defaultValue { mutableMapOf<String, OrientedBoundingBox>() }
        .serializer { it.serialize(OrientedBoundingBox.STRING_MAP_CODEC) }
        .build()

    @JvmStatic
    val PREINPUT = SparkCore.REGISTER.attachment<PreInput>()
        .id("preinput")
        .defaultValue { PreInput(it) }
        .build()

}