package cn.solarmoon.spark_core.api.phys.collision

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.model.part.CubePart
import cn.solarmoon.spark_core.api.data.SerializeHelper
import cn.solarmoon.spark_core.api.phys.getScaledAxisX
import cn.solarmoon.spark_core.api.phys.getScaledAxisY
import cn.solarmoon.spark_core.api.phys.getScaledAxisZ
import cn.solarmoon.spark_core.api.phys.toDegrees
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.client.Minecraft
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d.div
import thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d.toVec3
import java.awt.Color
import java.util.Random
import java.util.UUID
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.sqrt

// Y ^       8   +-------+   7     axisY   axisZ
//   |          /|      /|             | /
//   |     4   +-------+ | 3           |/
//   |  Z      | |     | |             +-- axisX
//   |   /   5 | +-----|-+  6       Center
//   |  /      |/      |/
//   | /   1   +-------+   2
//   |/
//   +--------------------> X
/**
 * 自由碰撞箱，比AABB更为自由，且有配套碰撞箱debug一套，爽飞
 * @param center 坐标轴原点位置，同时也是立方体的中心
 * @param size 以顺序为x，y，z轴坐标的正方向上的长度（整体长度，不是一半长度），输入0则此方块为一个点
 * @param rotation 坐标轴的整体旋转，对其进行变换则变换立方体的整体
 */
data class FreeCollisionBox(
    val center: Vector3f,
    val size: Vector3f = Vector3f(),
    val rotation: Quaternionf = Quaternionf()
) {

    private val halfSizeX get() = size.x / 2
    private val halfSizeY get() = size.y / 2
    private val halfSizeZ get() = size.z / 2

    // 缓存顶点
    var vertexes: Array<Vector3f> = arrayOf()

    init {
        updateVertices()
    }

    /**
     * 重新计算并返回立方体的八个顶点
     */
    fun updateVertices() {
        vertexes = arrayOf(
            transformVertex(Vector3f(-halfSizeX, -halfSizeY, -halfSizeZ)),
            transformVertex(Vector3f(halfSizeX, -halfSizeY, -halfSizeZ)),
            transformVertex(Vector3f(halfSizeX, halfSizeY, -halfSizeZ)),
            transformVertex(Vector3f(-halfSizeX, halfSizeY, -halfSizeZ)),
            transformVertex(Vector3f(-halfSizeX, -halfSizeY, halfSizeZ)),
            transformVertex(Vector3f(halfSizeX, -halfSizeY, halfSizeZ)),
            transformVertex(Vector3f(halfSizeX, halfSizeY, halfSizeZ)),
            transformVertex(Vector3f(-halfSizeX, halfSizeY, halfSizeZ))
        )
    }

    /**
     * 立方体的十二条边，以连接点表示
     */
    val connections = arrayOf(
        Pair(0, 1), Pair(0, 4), Pair(0, 3),
        Pair(5, 1), Pair(5, 4), Pair(5, 6),
        Pair(2, 1), Pair(2, 3), Pair(2, 6),
        Pair(7, 6), Pair(7, 3), Pair(7, 4),
    )

    /**
     * 将绝对坐标转为当前坐标轴的相对坐标
     *
     * 比如世界原点，应用转换后则为该box坐标系下的原点
     */
    fun transformVertex(vertex: Vector3f): Vector3f {
        rotation.transform(vertex)
        return vertex.add(center)
    }

    /**
     * 将中心在自身坐标系内偏移
     */
    fun offsetCenter(offset: Vector3f) = apply {
        val value = offset.rotate(rotation)
        center.add(value)
        updateVertices()
    }

    /**
     * 将该立方体随着指定生物的视野旋转，类似贴在生物眼睛上一样
     * @param partialTick 如果不在客户端输入1即可
     */
    fun transformWithEntityView(entity: Entity, partialTick: Float = 1f) {
        val qRot = Quaternionf().rotateY(Math.toRadians(-entity.getViewYRot(partialTick).toDouble()).toFloat()).rotateX(Math.toRadians(entity.getViewXRot(partialTick).toDouble()).toFloat())
        rotation.set(qRot)
        updateVertices()
    }

    fun FreeCollisionBox.intersects(box: FreeCollisionBox): Boolean {
        var axes = arrayOf(
            rotation.getScaledAxisX(), rotation.getScaledAxisY(), rotation.getScaledAxisZ(),
            box.rotation.getScaledAxisX(), box.rotation.getScaledAxisY(), box.rotation.getScaledAxisZ()
        )

        // 添加两个OBB的轴向叉积
        for (i in 0..2) {
            for (j in 0..2) {
                val axis = axes[i].cross(axes[j + 3], Vector3f()).normalize()
                if (axis.lengthSquared() > 1e-6) {
                    axes += axis
                }
            }
        }

        val centerVec = box.center.sub(center, Vector3f())

        fun projectOntoAxis(box: FreeCollisionBox, axis: Vector3f): Float {
            val halfSize = box.size.mul(0.5f, Vector3f())
            return abs(halfSize.x * axis.dot(rotation.getScaledAxisX())) +
                    abs(halfSize.y * axis.dot(rotation.getScaledAxisY())) +
                    abs(halfSize.z * axis.dot(rotation.getScaledAxisZ()))
        }

        for (axis in axes) {
            val projection1 = projectOntoAxis(this, axis)
            val projection2 = projectOntoAxis(box, axis)
            val distance = abs(centerVec.dot(axis))

            if (distance > projection1 + projection2) {
                return false // 找到分离轴，OBB不相交
            }
        }

        return true // 没找到分离轴，OBB相交
    }

    /**
     * @param box 要检测是否相交的box
     * @param boxCache 传入的缓存位置的box，如果此项不为null将在缓存的box中心位置和当前box中心位置之间进行分段插值，以分割出多个预测过渡box
     * @param densityScale 默认情况下，会根据两个中心点的距离自动分布合适数量的过渡框，但是如果觉得过多或过少可以调整此值
     * @return 返回与输入box相交的第一个box
     */
    fun connectionIntersects(box: FreeCollisionBox, boxCache: FreeCollisionBox?, densityScale: Double = 1.0): FreeCollisionBox? {
        if (boxCache != null) {
            val cCenter = boxCache.center
            val cSize = boxCache.size
            val cRotation = boxCache.rotation

            // 计算中心距离和旋转差异
            val posDistance = center.distance(cCenter).toInt()
            val rotDifference = abs(rotation.angle().toDegrees() - cRotation.angle().toDegrees()).toInt()

            // 计算最终 density，确保至少有一个插值框
            val totalDifference = posDistance + rotDifference / 10
            val finalDensity = maxOf((totalDifference * densityScale).toInt(), 1)

            // 遍历并生成插值框
            for (i in 1..finalDensity) {
                // 插值比例
                val t = i.toFloat() / finalDensity.toFloat()
                // 插值位置
                val interpolatedCenter = cCenter.lerp(center, t, Vector3f())
                // 插值大小
                val scaleDiffer = size.sub(cSize, Vector3f())
                val interpolatedSize = size.add(scaleDiffer.mul(t, Vector3f()), Vector3f())
                // 四元数插值
                val interpolatedRotation = cRotation.slerp(rotation, t, Quaternionf())
                // 创建当前分段的 box
                val interpolatedBox = FreeCollisionBox(interpolatedCenter, interpolatedSize, interpolatedRotation)

                // 调试并测试相交
                val debug = interpolatedBox.getRenderManager(UUID.randomUUID().toString(), 60)
                val flag = interpolatedBox.intersects(box)
                if (flag) debug.setHit()
                debug.sendRenderableBoxToClient()
                if (flag) return interpolatedBox
            }
        }

        // 如果没有返回任何插值框，则检查当前框与目标框的相交情况
        return if (intersects(box)) {
            getRenderManager(UUID.randomUUID().toString(), 60, Color.RED).sendRenderableBoxToClient()
            box
        } else null
    }

    fun getRenderManager(id: String, maxTime: Int = (5 / 0.02).toInt(), color: Color = Color.WHITE): FreeCollisionBoxRenderManager = FreeCollisionBoxRenderManager(id, this, maxTime, color)

    companion object {
        @JvmStatic
        val CODEC: Codec<FreeCollisionBox> = RecordCodecBuilder.create {
            it.group(
                SerializeHelper.VECTOR3F_CODEC.fieldOf("center").forGetter { it.center },
                SerializeHelper.VECTOR3F_CODEC.fieldOf("size").forGetter { it.size },
                SerializeHelper.QUATERNIONF_CODEC.fieldOf("rotation").forGetter { it.rotation }
            ).apply(it, ::FreeCollisionBox)
        }

        @JvmStatic
        val STREAM_CODEC = StreamCodec.composite(
            SerializeHelper.VECTOR3F_STREAM_CODEC, FreeCollisionBox::center,
            SerializeHelper.VECTOR3F_STREAM_CODEC, FreeCollisionBox::size,
            SerializeHelper.QUATERNIONF_STREAM_CODEC, FreeCollisionBox::rotation,
            ::FreeCollisionBox
        )
    }

}
