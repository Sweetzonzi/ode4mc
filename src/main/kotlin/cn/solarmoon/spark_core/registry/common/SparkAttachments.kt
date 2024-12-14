package cn.solarmoon.spark_core.registry.common

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.anim.play.AnimData
import cn.solarmoon.spark_core.api.entity.attack.AttackedData
import cn.solarmoon.spark_core.api.entity.preinput.PreInput
import cn.solarmoon.spark_core.api.phys.EntityBoundingBoxBone
import cn.solarmoon.spark_core.api.phys.IBoundingBone
import cn.solarmoon.spark_core.api.phys.thread.PhysLevel
import net.minecraft.world.entity.Entity
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
        .build()

    @JvmStatic
    val BOUNDING_BONES = SparkCore.REGISTER.attachment<MutableMap<String, IBoundingBone>>()
        .id("bounding_bones")
        .defaultValue {
            val value = mutableMapOf<String, IBoundingBone>()
            when(it) {
                is Entity -> value["body"] = EntityBoundingBoxBone(it, "body")
            }
            value
        }
        .build()

    @JvmStatic
    val PREINPUT = SparkCore.REGISTER.attachment<PreInput>()
        .id("preinput")
        .defaultValue { PreInput(it) }
        .build()

    @JvmStatic
    val PHYS_LEVEL = SparkCore.REGISTER.attachment<Optional<PhysLevel>>()
        .id("phys_level")
        .defaultValue { Optional.empty() }
        .build()

}