package cn.solarmoon.spark_core.api.animation.anim

import cn.solarmoon.spark_core.api.animation.sync.AnimNetData
import cn.solarmoon.spark_core.api.phys.toRadians
import cn.solarmoon.spark_core.registry.common.SparkAttachments
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.network.PacketDistributor
import org.joml.Matrix4f
import thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d.div
import thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d.toVec3
import kotlin.math.PI

/**
 * 所有动画模型数据在服务端的生物都需要接入此接口
 *
 * 该接口提供多种方法可以便捷地定位当前生物的模型上的各个点在世界中的位置等等
 */
interface IAnimatable<T: Entity> {

    /**
     * 一般而言输入this即可，用于调用该生物的位置信息等
     */
    val animatable: T

    /**
     * 动画控制器，可以轻松对动画执行开始/停止等操作，并且这些操作都是服务端可用的
     */
    val animController: AnimController<IAnimatable<T>>

    var animData: AnimData
        get() = animatable.getData(SparkAttachments.ANIM_DATA)
        set(value) { animatable.setData(SparkAttachments.ANIM_DATA, value) }

    /**
     * 获取当前实体所在位置的矩阵
     */
    fun getEntityMatrix(partialTick: Float = -1f): Matrix4f {
        val p = if (partialTick == -1f) 1f else partialTick // 修正一下客户端tick，不然无法和apply里的partialTick统一
        return Matrix4f().translate(animatable.getPosition(p).toVector3f()).rotateY(PI.toFloat() - animatable.getPreciseBodyRotation(partialTick).toRadians())
    }

    /**
     * 获取该生物指定骨骼在世界坐标系中的枢轴点
     */
    fun getBonePivot(name: String, partialTick: Float = -1f): Vec3 {
        val p = if (partialTick == -1f) 0f else partialTick // 修正一下客户端tick，不然无法和apply里的partialTick统一
        val ma = getEntityMatrix(partialTick)
        val bone = animData.model.getBone(name)
        val headRot = getHeadMatrix(partialTick)
        bone.applyTransformWithParents(animData, ma, headRot, p)
        val pivot = bone.pivot.div(16.0).toVector3f()
        return ma.transformPosition(pivot).toVec3()
    }

    /**
     * 获取指定骨骼的变换
     */
    fun getBoneMatrix(name: String, partialTick: Float = -1f): Matrix4f {
        val p = if (partialTick == -1f) 0f else partialTick // 修正一下客户端tick，不然无法和apply里的partialTick统一
        val ma = getEntityMatrix(partialTick)
        val bone = animData.model.getBone(name)
        val headRot = getHeadMatrix(partialTick)
        bone.applyTransformWithParents(animData, ma, headRot, p)
        return ma
    }

    /**
     * 获取头部视野的旋转矩阵
     */
    fun getHeadMatrix(partialTick: Float = 0f): Matrix4f {
        val pitch = -animatable.getViewXRot(partialTick).toRadians()
        val yaw = -animatable.getViewYRot(partialTick).toRadians() + animatable.getPreciseBodyRotation(partialTick).toRadians()
        return Matrix4f().rotateZYX(0f, yaw, pitch)
    }

    /**
     * 同步当前动画数据到客户端，默认每tick都会调用一次
     * @param placeAnyCase 是否只在动画名字不同时发送数据，反之则无论如何也要同步
     */
    fun syncAnimDataToClient(placeAnyCase: Boolean = false) {
        if (!animatable.level().isClientSide) {
            PacketDistributor.sendToAllPlayers(AnimNetData(animatable.id, animData, placeAnyCase))
        }
    }

}