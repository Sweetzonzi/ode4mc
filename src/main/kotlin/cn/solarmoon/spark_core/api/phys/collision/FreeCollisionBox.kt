package cn.solarmoon.spark_core.api.phys.collision

import cn.solarmoon.spark_core.api.data.SerializeHelper
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d.toVec3

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
 *
 * 同时可以和客户端进行同步
 * @param center 坐标轴原点位置，同时也是立方体的中心
 * @param rotation 坐标轴的整体旋转，对其进行变换则变换立方体的整体
 * @param size 以顺序为x，y，z轴坐标的正方向上的长度（整体长度，不是一半长度）
 */
data class FreeCollisionBox(//
    var center: Vec3,
    var size: Vec3,
    val rotation: Quaternionf = Quaternionf()
) {

    private val halfSizeX get() = size.x / 2
    private val halfSizeY get() = size.y / 2
    private val halfSizeZ get() = size.z / 2

    /**
     * 立方体的八个顶点，顺序见此类头部注释
     */
    val vertexes get() = arrayOf(
        transformVertex(Vec3(-halfSizeX, -halfSizeY, -halfSizeZ)),
        transformVertex(Vec3(halfSizeX, -halfSizeY, -halfSizeZ)),
        transformVertex(Vec3(halfSizeX, halfSizeY, -halfSizeZ)),
        transformVertex(Vec3(-halfSizeX, halfSizeY, -halfSizeZ)),
        transformVertex(Vec3(-halfSizeX, -halfSizeY, halfSizeZ)),
        transformVertex(Vec3(halfSizeX, -halfSizeY, halfSizeZ)),
        transformVertex(Vec3(halfSizeX, halfSizeY, halfSizeZ)),
        transformVertex(Vec3(-halfSizeX, halfSizeY, halfSizeZ))
    )

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
     */
    fun transformVertex(vertex: Vec3): Vec3 {
        val vector = vertex.toVector3f()
        rotation.transform(vector)
        return vector.add(center.toVector3f()).toVec3()
    }

    /**
     * 将该立方体随着指定生物的视野旋转，类似贴在生物眼睛上一样
     * @param partialTick 如果不在客户端输入1即可
     */
    fun transformWithEntityView(entity: Entity, partialTick: Float = 1f) {
        val qRot = Quaternionf().rotateY(Math.toRadians(-entity.getViewYRot(partialTick).toDouble()).toFloat()).rotateX(Math.toRadians(entity.getViewXRot(partialTick).toDouble()).toFloat())
        rotation.set(qRot)
    }

    /**
     * 两个碰撞箱是否有相交部分，利用两者的三条坐标轴来进行比较，三条轴都有相交部分，则相交
     */
    fun intersects(box: FreeCollisionBox): Boolean {
        val min1 = center.subtract(size.scale(0.5))
        val max1 = center.add(size.scale(0.5))
        val min2 = box.center.subtract(box.size.scale(0.5))
        val max2 = box.center.add(box.size.scale(0.5))

        return (min1.x <= max2.x && max1.x >= min2.x) &&
                (min1.y <= max2.y && max1.y >= min2.y) &&
                (min1.z <= max2.z && max1.z >= min2.z)
    }

    companion object {
        @JvmStatic
        fun of(aabb: AABB): FreeCollisionBox {
            val center = Vec3(
                (aabb.minX + aabb.maxX) / 2,
                (aabb.minY + aabb.maxY) / 2,
                (aabb.minZ + aabb.maxZ) / 2
            )
            val size = Vec3(
                aabb.maxX - aabb.minX,
                aabb.maxY - aabb.minY,
                aabb.maxZ - aabb.minZ
            )
            return FreeCollisionBox(center, size)
        }

        @JvmStatic
        val STREAM_CODEC = StreamCodec.composite(
            SerializeHelper.VEC3_STREAM_CODEC, FreeCollisionBox::center,
            SerializeHelper.VEC3_STREAM_CODEC, FreeCollisionBox::size,
            SerializeHelper.QUATERNIONF_STREAM_CODEC, FreeCollisionBox::rotation,
            ::FreeCollisionBox
        )
    }

}
