package cn.solarmoon.spark_core.api.entity.state

import cn.solarmoon.spark_core.api.phys.toRadians
import cn.solarmoon.spark_core.api.util.Side
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d.toVec3
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object EntityStateHelper {

    @JvmStatic
    val DATA_STATE_FLAGS_ID: Lazy<EntityDataAccessor<Byte>> = lazy { SynchedEntityData.defineId(Entity::class.java, EntityDataSerializers.BYTE) }

}

fun Entity.moveCheck(): Boolean {
    val v = knownMovement
    val avgV = (abs(v.x) + abs(v.z)) / 2f
    return avgV >= 0.015
}

fun Entity.moveBackCheck(): Boolean {
    val v = knownMovement
    val forward = Vec3.directionFromRotation(0f, getPreciseBodyRotation(1f))
    // 计算移动的标量与 身体forward 方向的点积，如果乘数大于150度值则代表方向基本相反
    val dotProduct = v.normalize().x * forward.normalize().x + v.normalize().z * forward.normalize().z
    return dotProduct < cos(120f.toRadians()) && (abs(v.x) + abs(v.z)) >= 0.015
}

fun Entity.isMoving(): Boolean {
    return getState(1)
}

fun Entity.setMoving(set: Boolean) {
    setState(1, set)
}

fun Entity.isMovingBack(): Boolean {
    return getState(2)
}

fun Entity.setMovingBack(set: Boolean) {
    setState(2, set)
}

fun Entity.isJumping(): Boolean {
    return getState(3)
}

fun Entity.setJumpingState(set: Boolean) {
    setState(3, set)
}

fun Entity.setState(flag: Int, set: Boolean) {
    val b0 = this.entityData[EntityStateHelper.DATA_STATE_FLAGS_ID.value].toInt()
    if (set) {
        this.entityData[EntityStateHelper.DATA_STATE_FLAGS_ID.value] = (b0 or (1 shl flag)).toByte()
    } else {
        this.entityData[EntityStateHelper.DATA_STATE_FLAGS_ID.value] = (b0 and (1 shl flag).inv()).toByte()
    }
}

fun Entity.getState(flag: Int): Boolean {
    return (this.entityData.get(EntityStateHelper.DATA_STATE_FLAGS_ID.value).toInt() and (1 shl flag)) != 0
}

fun Entity.isFalling(): Boolean {
    return !onGround() && deltaMovement.y != 0.0
}

/**
 * 判断目标实体是否在输入实体朝向的一个扇形角度范围内（输入量都是角度制）
 * @param targetPos 目标实体位置
 * @param rotateY 将扇形区域绕目标中心点进行整体旋转
 */
fun Entity.isInRangeFrontOf(targetPos: Vec3, rangeDegrees: Double, rotateY: Float = 0f): Boolean {
    val entityPos = position()

    // 朝向
    val viewVector = Vec3.directionFromRotation(0f, yRot).toVector3f().rotateY(rotateY.toRadians()).toVec3()

    // 计算实体到目标实体的向量
    val directionToTarget = targetPos.subtract(entityPos).normalize()

    // 计算视线向量和方向向量的点积
    val dotProduct = viewVector.dot(directionToTarget)

    // 夹角的余弦值
    val thresholdCosAngle = cos(Math.toRadians(rangeDegrees / 2))

    // 判断夹角是否小于指定度数
    return dotProduct > thresholdCosAngle
}

/**
 * 根据扇形角度划分该实体在目标实体的哪个方向，其中前后为135度扇形，左右为45度扇形
 */
fun Entity.getSideOf(targetPos: Vec3): Side {
    return when {
        isInRangeFrontOf(targetPos, 135.0, 0f) -> Side.FRONT
        isInRangeFrontOf(targetPos, 45.0, 90f) -> Side.LEFT
        isInRangeFrontOf(targetPos, 135.0, 180f) -> Side.BACK
        isInRangeFrontOf(targetPos, 45.0, 270f) -> Side.RIGHT
        else -> Side.FRONT
    }
}

/**
 * 根据玩家客户端输入和玩家自身朝向输出玩家的移动速度向量
 */
@OnlyIn(Dist.CLIENT)
fun LocalPlayer.getInputVector(): Vec3 {
    val v = input.moveVector.normalized()
    val f2 = v.x
    val f3 = v.y
    val f4 = sin(yRot * (PI / 180.0))
    val f5 = cos(yRot * (PI / 180.0))
    return Vec3((f2 * f5 - f3 * f4), deltaMovement.y, (f3 * f5 + f2 * f4))
}
