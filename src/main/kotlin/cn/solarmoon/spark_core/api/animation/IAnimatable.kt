package cn.solarmoon.spark_core.api.animation

import cn.solarmoon.spark_core.api.animation.anim.part.Animation
import cn.solarmoon.spark_core.api.animation.anim.play.AnimController
import cn.solarmoon.spark_core.api.animation.anim.play.AnimData
import cn.solarmoon.spark_core.api.animation.sync.AnimNetData
import cn.solarmoon.spark_core.api.phys.collision.FreeCollisionBox
import cn.solarmoon.spark_core.registry.common.SparkAttachments
import net.neoforged.neoforge.attachment.IAttachmentHolder
import net.neoforged.neoforge.network.PacketDistributor
import org.joml.Matrix4f
import org.joml.Vector3f

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
    fun getPositionMatrix(partialTick: Float = 0f): Matrix4f

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
    fun createCollisionBoxBoundToBone(boneName: String, size: Vector3f = Vector3f(), offset: Vector3f = Vector3f()): FreeCollisionBox {
        return FreeCollisionBox(getBonePivot(boneName), size)
            .apply { rotation.setFromUnnormalized(getBoneMatrix(boneName)) }
            .apply { offsetCenter(offset) }
    }

    /**
     * 同步当前动画播放和定位的必要数据到客户端
     */
    fun syncAnimDataToClient() {
        PacketDistributor.sendToAllPlayers(AnimNetData(0, animData))
    }

    /**
     * 在使用[cn.solarmoon.spark_core.api.entity.ai.attack.AttackHelper.getDamageBone]时，将会过滤掉该列表中的骨骼，也就是不会击中这些过滤掉的骨骼
     */
    val passableBones: List<String> get() = listOf()

}