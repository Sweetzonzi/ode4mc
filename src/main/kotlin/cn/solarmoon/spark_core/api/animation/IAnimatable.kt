package cn.solarmoon.spark_core.api.animation

import cn.solarmoon.spark_core.api.animation.anim.play.AnimController
import cn.solarmoon.spark_core.api.animation.anim.play.AnimData
import cn.solarmoon.spark_core.api.animation.anim.auto_anim.AutoAnim
import cn.solarmoon.spark_core.api.animation.sync.AnimDataPayload
import cn.solarmoon.spark_core.api.phys.obb.OrientedBoundingBox
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.network.PacketDistributor
import org.joml.Matrix4f
import org.joml.Vector3f

interface IAnimatable<T> {

    /**
     * 一般而言输入this即可，用于调用该生物的位置信息等
     */
    val animatable: T

    /**
     * 动画控制器，可以轻松对动画执行开始/停止等操作，并且这些操作都是服务端可用的
     */
    val animController: AnimController<IAnimatable<T>>

    var animData: AnimData

    /**
     * 自动播放的动画，但是此处直接传入一个空列表即可，将会在生物初始化时自动加入所有的自动动画，并且不要写成getter形式以免动画无法加入
     */
    val autoAnims: MutableSet<AutoAnim<*>>

    /**
     * 获取位移到当前坐标并应用了基础旋转的变换矩阵
     */
    fun getPositionMatrix(partialTick: Float = 1f): Matrix4f

    /**
     * 对指定骨骼进行的额外变换
     */
    fun getExtraTransform(partialTick: Float = 1f): Map<String, Matrix4f> = mapOf()

    /**
     * 获取该生物指定骨骼在世界坐标系中的枢轴点
     */
    fun getBonePivot(name: String, partialTick: Float = 1f): Vector3f

    /**
     * 获取指定骨骼的变换
     */
    fun getBoneMatrix(name: String, partialTick: Float = 1f): Matrix4f

    /**
     * 创建和骨骼绑定的碰撞箱
     */
    fun createCollisionBoxBoundToBone(boneName: String, size: Vector3f = Vector3f(), offset: Vector3f = Vector3f(), partialTicks: Float = 1f): OrientedBoundingBox {
        return OrientedBoundingBox(getBonePivot(boneName, partialTicks), size)
            .apply { rotation.setFromUnnormalized(getBoneMatrix(boneName)) }
            .apply { offsetCenter(offset) }
    }

    /**
     * 同步当前动画播放和定位的所有必要数据到客户端
     * @param playerExcept 如果不希望发送数据包到当前玩家，输入一个非null玩家值即可（一般此玩家和本地玩家一致）
     */
    fun syncAnimDataToClient(playerExcept: ServerPlayer? = null) {
        val data = AnimDataPayload(0, animData.copy())
        playerExcept?.let { PacketDistributor.sendToPlayersNear(it.serverLevel(), it, it.x, it.y, it.z, 512.0, data) }
            ?: run {
                PacketDistributor.sendToAllPlayers(data)
            }
    }

}