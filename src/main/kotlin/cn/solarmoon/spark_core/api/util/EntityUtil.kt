package cn.solarmoon.spark_core.api.util

import net.minecraft.world.entity.Entity
import kotlin.math.cos

object EntityUtil {

    /**
     * @param rangeDegrees 以entity视线为中线，向左右任一侧偏转的度数，比如希望视野范围是眼前90度扇形，则填入45即可
     */
    @JvmStatic
    fun canSee(entity: Entity, target: Entity?, rangeDegrees: Double): Boolean {
        if (target == null) return false
        // 获取实体的视线向量
        val viewVector = entity.getViewVector(1.0f)

        // 获取实体和目标实体的位置向量
        val entityPos = entity.position()
        val targetPos = target.position()

        // 计算实体到目标实体的向量
        val directionToTarget = targetPos.subtract(entityPos).normalize()

        // 计算视线向量和方向向量的点积
        val dotProduct = viewVector.dot(directionToTarget)

        // 计算夹角的余弦值
        val cosAngle = dotProduct / (viewVector.length() * directionToTarget.length())

        // 判断夹角是否小于指定度
        val thresholdCosAngle = cos(Math.toRadians(rangeDegrees))
        return cosAngle > thresholdCosAngle
    }

}