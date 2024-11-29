package cn.solarmoon.spark_core.api.animation

import cn.solarmoon.spark_core.api.animation.anim.play.AnimController
import cn.solarmoon.spark_core.api.animation.anim.play.AnimData
import cn.solarmoon.spark_core.api.animation.sync.AnimDataPayload
import cn.solarmoon.spark_core.api.phys.obb.OrientedBoundingBox
import cn.solarmoon.spark_core.registry.common.SparkAttachments
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Items
import net.neoforged.neoforge.attachment.IAttachmentHolder
import net.neoforged.neoforge.network.PacketDistributor
import org.joml.Matrix4f
import org.joml.Vector3f
import kotlin.properties.Delegates

interface IAnimatable<T: IAttachmentHolder> {

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
     * 获取位移到当前坐标并应用了基础旋转的变换矩阵
     */
    fun getPositionMatrix(partialTick: Float = 1f): Matrix4f

    /**
     * 对指定骨骼进行的额外变换
     */
    fun getExtraTransform(partialTick: Float = 0f): Map<String, Matrix4f> = mapOf()

    /**
     * 获取该生物指定骨骼在世界坐标系中的枢轴点
     */
    fun getBonePivot(name: String, partialTick: Float = 0f): Vector3f

    /**
     * 获取指定骨骼的变换
     */
    fun getBoneMatrix(name: String, partialTick: Float = 0f): Matrix4f

    /**
     * 创建和骨骼绑定的碰撞箱
     */
    fun createCollisionBoxBoundToBone(boneName: String, size: Vector3f = Vector3f(), offset: Vector3f = Vector3f(), partialTicks: Float = 0f): OrientedBoundingBox {
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

    /**
     * 在使用[cn.solarmoon.spark_core.api.entity.attack.AttackHelper.getDamageBone]时，将会过滤掉该列表中的骨骼，也就是不会击中这些过滤掉的骨骼
     */
    val passableBones: List<String> get() = listOf()

}