package cn.solarmoon.spark_core.api.util

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

object VecUtil {

    /**
     * 计算生物面前一段距离的坐标位置，高度默认和玩家视线齐平
     */
    @JvmStatic
    fun getSpawnPosFrontEntity(living: LivingEntity, distanceInFront: Double): Vec3 {
        val lookVec = living.lookAngle
        val inFrontVec = lookVec.scale(distanceInFront)
        return living.position().add(0.0, living.eyeHeight.toDouble(), 0.0).add(inFrontVec)
    }

    /**
     * 计算生物面前一段距离的坐标位置，且能调整高度
     */
    @JvmStatic
    fun getSpawnPosFrontEntity(living: LivingEntity, distanceInFront: Double, yOffset: Double): Vec3 {
        val lookVec = living.lookAngle
        val inFrontVec = lookVec.scale(distanceInFront)
        return living.position().add(0.0, living.eyeHeight.toDouble() + yOffset, 0.0).add(inFrontVec)
    }

    /**
     * 把输入的vec以输入的center为中心按照direction的角度进行旋转（一般用于需要在方块上创建相对固定坐标的情况）
     */
    @JvmStatic
    fun rotateVec(point: Vec3, center: Vec3, direction: Direction): Vec3 {
        // 转换为相对坐标
        val relative = point.subtract(center)
        // 获取旋转角度
        val angle = Math.toRadians(direction.toYRot().toDouble())
        // 计算旋转矩阵
        val sin = sin(angle)
        val cos = cos(angle)
        // 旋转相对坐标
        val x = relative.x * cos - relative.z * sin
        val z = relative.x * sin + relative.z * cos
        // 转换回绝对坐标
        return Vec3(x, relative.y, z).add(center)
    }

    /**
     * 判断某个点是否在一个矩形范围内
     * @param point 落点
     * @param rangePoint1 矩形对角坐标1
     * @param rangePoint2 矩形对角坐标2
     */
    @JvmStatic
    fun inRange(point: Vec3, rangePoint1: Vec3, rangePoint2: Vec3): Boolean {
        val x1 = min(rangePoint1.x, rangePoint2.x)
        val x2 = max(rangePoint1.x, rangePoint2.x)
        val y1 = min(rangePoint1.y, rangePoint2.y)
        val y2 = max(rangePoint1.y, rangePoint2.y)
        val z1 = min(rangePoint1.z, rangePoint2.z)
        val z2 = max(rangePoint1.z, rangePoint2.z)
        return point.x in x1..x2 && y1 <= point.y && point.y <= y2 && z1 <= point.z && point.z <= z2 //立体矩形范围判别
    }

    /**
     * @param vec 落点
     * @param pos 比较的方块坐标
     * @return 判断vec3落点是否在这个pos所在1x1x1大小的方块的内部
     */
    @JvmStatic
    fun isInside(vec: Vec3, pos: BlockPos): Boolean {
        val x = vec.x - pos.x
        val y = vec.y - pos.y
        val z = vec.z - pos.z
        return x > 0 && x < 1 && y > 0 && y < 1 && z > 0 && z < 1
    }

    /**
     * @param vec 落点
     * @param pos 比较的方块坐标
     * @return 判断vec3落点是否在这个pos所在指定顶点大小的方块的内部
     */
    @JvmStatic
    fun isInside(
        vec: Vec3,
        pos: BlockPos,
        xMin: Double,
        yMin: Double,
        zMin: Double,
        xMax: Double,
        yMax: Double,
        zMax: Double,
        border: Boolean
    ): Boolean {
        val x = vec.x - pos.x
        val y = vec.y - pos.y
        val z = vec.z - pos.z
        if (border) return x in xMin..xMax && y >= yMin && y <= yMax && z >= zMin && z <= zMax
        return x > xMin && x < xMax && y > yMin && y < yMax && z > zMin && z < zMax
    }

}