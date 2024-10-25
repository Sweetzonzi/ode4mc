package cn.solarmoon.spark_core.api.entity

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.phys.collision.FreeCollisionBox
import cn.solarmoon.spark_core.api.phys.collision.toOBB
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import java.awt.Color
import java.util.UUID
import kotlin.math.abs

object EntityStateHelper {

    /**
     * 是否正在移动
     */
    @JvmStatic
    fun isMoving(entity: Entity): Boolean {
        val limb = if (entity is LivingEntity) entity.walkAnimation.speed() else 1f
        val v = entity.deltaMovement
        val avgV = (abs(v.x) + abs(v.z)) / 2f
        return avgV >= 0.015 && limb != 0f
    }

}