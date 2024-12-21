package cn.solarmoon.spark_core.registry.common

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.phys.DxAnimAttackEntity
import cn.solarmoon.spark_core.api.phys.DxBoundingBoxEntity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory

object SparkEntityTypes {

    @JvmStatic
    fun register() {}

    @JvmStatic
    val DX_BOUNDING_BOX = SparkCore.REGISTER.entityType<DxBoundingBoxEntity>()
        .id("dx_bounding_box")
        .builder(EntityType.Builder.of(::DxBoundingBoxEntity, MobCategory.MISC).sized(0f, 0f).eyeHeight(0f))
        .build()

    @JvmStatic
    val DX_ANIM_ATTACK = SparkCore.REGISTER.entityType<DxAnimAttackEntity>()
        .id("dx_anim_attack")
        .builder(EntityType.Builder.of(::DxAnimAttackEntity, MobCategory.MISC).sized(0f, 0f).eyeHeight(0f))
        .build()

}