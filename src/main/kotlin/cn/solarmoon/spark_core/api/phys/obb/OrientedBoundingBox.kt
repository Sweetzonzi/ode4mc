package cn.solarmoon.spark_core.api.phys.obb

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.data.SerializeHelper
import cn.solarmoon.spark_core.api.phys.copy
import cn.solarmoon.spark_core.api.phys.getScaledAxisX
import cn.solarmoon.spark_core.api.phys.getScaledAxisY
import cn.solarmoon.spark_core.api.phys.getScaledAxisZ
import cn.solarmoon.spark_core.api.phys.toDegrees
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Direction
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import org.joml.Quaternionf
import org.joml.Vector3f
import thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d.toVec3
import kotlin.math.abs

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
data class OrientedBoundingBox(
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

    fun getAxisFaceCenters(axis: Direction.Axis): Pair<Vector3f, Vector3f> {
        return when(axis) {
            Direction.Axis.X -> {
                val leftCenter = vertexes[0].lerp(vertexes[7], 0.5f, Vector3f())
                val rightCenter = vertexes[1].lerp(vertexes[6], 0.5f, Vector3f())
                Pair(leftCenter, rightCenter)
            }
            Direction.Axis.Y -> {
                val bottomCenter = vertexes[0].lerp(vertexes[5], 0.5f, Vector3f())
                val topCenter = vertexes[3].lerp(vertexes[6], 0.5f, Vector3f())
                Pair(bottomCenter, topCenter)
            }
            Direction.Axis.Z -> {
                val backCenter = vertexes[0].lerp(vertexes[2], 0.5f, Vector3f())
                val frontCenter = vertexes[4].lerp(vertexes[6], 0.5f, Vector3f())
                Pair(backCenter, frontCenter)
            }
        }
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
        val value = offset.rotate(rotation, Vector3f())
        center.add(value)
        updateVertices()
    }

    /**
     * 扩张方块大小
     */
    fun inflate(sizeAddon: Vector3f) = apply {
        size.add(sizeAddon)
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

    fun intersects(box: OrientedBoundingBox): Boolean {
        val axes = mutableListOf<Vector3f>().apply {
            add(rotation.getScaledAxisX())
            add(rotation.getScaledAxisY())
            add(rotation.getScaledAxisZ())
            add(box.rotation.getScaledAxisX())
            add(box.rotation.getScaledAxisY())
            add(box.rotation.getScaledAxisZ())

            // 添加两个OBB的轴向叉积
            for (i in 0..2) {
                for (j in 0..2) {
                    val axis = this[i].cross(this[j + 3], Vector3f()).normalize()
                    if (axis.lengthSquared() > 1e-6) {
                        add(axis)
                    }
                }
            }
        }

        val centerVec = Vector3f(box.center).sub(center)

        fun projectOntoAxis(box: OrientedBoundingBox, axis: Vector3f): Float {
            val halfSize = box.size.mul(0.5f, Vector3f())
            return abs(halfSize.x * axis.dot(box.rotation.getScaledAxisX())) +
                    abs(halfSize.y * axis.dot(box.rotation.getScaledAxisY())) +
                    abs(halfSize.z * axis.dot(box.rotation.getScaledAxisZ()))
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
     * ### 此方法为预测相交判断，会在输入的缓存的box和当前box之间生成线性差值过渡的box，用于一些特定的情况比如高速运动时box丢帧的情况。
     *
     * 但此方法无法准确模拟真实的运动轨迹，尤其是对于两点间的位移，它无法判断两点间具体位移的路径，只能将两点简单的连成一条线。
     * 因此涉及到超高速运动的情况，首先建议您直接根据高速运动的路径创建一个大的碰撞箱来作为判定，可以省去很多麻烦，而且效果上也不会差。
     *
     * #### 具体来说，以下情况可能不适用此方法的预测box：
     * - 缓存box和当前box之间的调用间隔小于1tick（简单来说就是还没有更新缓存box的情况当前调用已经结束了，那么显然这时候无法生成预测box，因为缓存box还没来得及输入，除非并非在tick中调用，总之保证缓存box即时输入即可）
     * - 每tick之间的路径变换很复杂，并非可以近似为一条直线（旋转由于路径简单，因此一般容易预测，但是位置在1tick间行进路线过于复杂时，只会生成直线的预测路径）
     * @param box 要检测是否相交的box，注意它只用来判断是否相交，预测box的生成只与当前box和boxCache决定
     * @param boxCache 传入的缓存位置的box，如果此项不为null将在缓存的box中心位置和当前box中心位置之间进行分段插值，以分割出多个预测过渡box
     * @param densityScale 默认情况下，会根据两个中心点的距离自动分布合适数量的过渡框，但是如果觉得过多或过少可以调整此值
     * @return 返回与输入box相交的第一个box
     */
    fun connectionIntersects(box: OrientedBoundingBox, boxCache: OrientedBoundingBox?, densityScale: Double = 1.0): OrientedBoundingBox? {
        if (boxCache != null) {
            val cCenter = boxCache.center
            val cRotation = boxCache.rotation

            // 计算中心距离和旋转差异
            val posDistance = center.distance(cCenter).times(10).toInt()
            val rotDifference = abs(rotation.angle().toDegrees() - cRotation.angle().toDegrees()).toInt()

            // 计算最终 density，确保至少有一个插值框
            val totalDifference = posDistance + rotDifference / 10
            val finalDensity = maxOf((totalDifference * densityScale).toInt(), 1)

            // 遍历并生成插值框
            for (i in 1..finalDensity) {
                // 插值比例
                val t = i.toFloat() / finalDensity.toFloat()
                // 创建当前分段的 box
                val interpolatedBox = boxCache.lerp(t, this)

                // 调试并测试相交
                //SparkVisualEffectRenderers.OBB.syncBoxToClient(UUID.randomUUID().toString(), Color.YELLOW, interpolatedBox)
                if (interpolatedBox.intersects(box)) return interpolatedBox
            }
        }

        // 如果没有返回任何插值框，则检查当前框与目标框的相交情况
        return if (intersects(box)) this else null
    }

    fun lerp(progress: Float, boxTo: OrientedBoundingBox): OrientedBoundingBox {
        val lerpedCenter = center.lerp(boxTo.center, progress, Vector3f())
        val lerpedScale = size.lerp(boxTo.size, progress, Vector3f())
        val lerpedRotation = rotation.slerp(boxTo.rotation, progress, Quaternionf())
        return OrientedBoundingBox(lerpedCenter, lerpedScale, lerpedRotation)
    }

    /**
     * 按生物的触及距离进行碰撞箱范围扩张
     */
    fun extendByEntityInteractRange(entity: Entity, omniExpansion: Boolean = false): OrientedBoundingBox = apply {
        if (entity is LivingEntity) {
            entity.getAttribute(Attributes.ENTITY_INTERACTION_RANGE)?.let {
                var differ = it.baseValue.toFloat()
                if (entity is Player && entity.isCreative) differ += 2
                (it.value.toFloat() - differ).takeIf { it > 0 }?.let { add ->
                    inflate(if (omniExpansion) Vector3f(add) else Vector3f(0f, 0f, add))
                }
            }
        }
    }

    fun copy(): OrientedBoundingBox {
        return OrientedBoundingBox(center.copy(), size.copy(), rotation.copy())
    }

    companion object {
        @JvmStatic
        val ZERO get() = OrientedBoundingBox(Vector3f())

        @JvmStatic
        val CODEC: Codec<OrientedBoundingBox> = RecordCodecBuilder.create {
            it.group(
                ExtraCodecs.VECTOR3F.fieldOf("center").forGetter { it.center },
                ExtraCodecs.VECTOR3F.fieldOf("size").forGetter { it.size },
                ExtraCodecs.QUATERNIONF.fieldOf("rotation").forGetter { it.rotation }
            ).apply(it, ::OrientedBoundingBox)
        }

        @JvmStatic
        val STRING_MAP_CODEC = Codec.unboundedMap(Codec.STRING, CODEC).xmap(
            { it.toMutableMap() },
            { it.toMutableMap() }
        )

        @JvmStatic
        val STREAM_CODEC = StreamCodec.composite(
            SerializeHelper.VECTOR3F_STREAM_CODEC, OrientedBoundingBox::center,
            SerializeHelper.VECTOR3F_STREAM_CODEC, OrientedBoundingBox::size,
            SerializeHelper.QUATERNIONF_STREAM_CODEC, OrientedBoundingBox::rotation,
            ::OrientedBoundingBox
        )
    }

}
