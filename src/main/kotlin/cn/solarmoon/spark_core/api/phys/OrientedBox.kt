package cn.solarmoon.spark_core.api.phys

import cn.solarmoon.spark_core.api.util.VecUtil
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Matrix3f
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Credit: <a href="https://github.com/ZsoltMolnarrr/BetterCombat/tree/1.20.4">...</a>
 */
// Y ^       8   +-------+   7     axisY   axisZ
//   |          /|      /|             | /
//   |     4   +-------+ | 3           |/
//   |  Z      | |     | |             +-- axisX
//   |   /   5 | +-----|-+  6       Center
//   |  /      |/      |/
//   | /   1   +-------+   2
//   |/
//   +--------------------> X
data class OrientedBox(
    var center: Vec3,
    var extent: Vec3,
    var axisX: Vec3,
    var axisY: Vec3,
    var axisZ: Vec3
) {
    constructor(center: Vec3, width: Double, height: Double, depth: Double, yaw: Float, pitch: Float): this(
        center,
        Vec3(width / 2.0, height / 2.0, depth / 2.0),
        Vec3(0.0, 0.0, 0.0),
        Vec3(0.0, 0.0, 0.0),
        Vec3(0.0, 0.0, 0.0)
    ) {
        axisZ = Vec3.directionFromRotation(yaw, pitch).normalize()
        axisY = Vec3.directionFromRotation(yaw + 90, pitch).normalize()
        axisX = axisZ.cross(axisY)
    }

    /**
     * 直接在玩家面前生成一个矩形框
     */
    constructor(entity: LivingEntity, width: Double, height: Double, depth: Double, distanceInFrontPlayer: Double = depth / 2): this(
        VecUtil.getSpawnPosFrontEntity(entity, distanceInFrontPlayer),
        width, height, depth, entity.xRot, entity.yRot
    )

    constructor(box: AABB): this(
        Vec3((box.maxX + box.minX) / 2.0, (box.maxY + box.minY) / 2.0, (box.maxZ + box.minZ) / 2.0),
        Vec3(abs(box.maxX - box.minX) / 2.0, abs(box.maxY - box.minY) / 2.0, abs(box.maxZ - box.minZ) / 2.0),
        Vec3(1.0, 0.0, 0.0),
        Vec3(0.0, 1.0, 0.0),
        Vec3(0.0, 0.0, 1.0)
    )

    // DERIVED PROPERTIES
    var scaledAxisX: Vec3 = Vec3.ZERO
    var scaledAxisY: Vec3 = Vec3.ZERO
    var scaledAxisZ: Vec3 = Vec3.ZERO
    var rotation: Matrix3f = Matrix3f()
    var vertex1: Vec3 = Vec3.ZERO
    var vertex2: Vec3 = Vec3.ZERO
    var vertex3: Vec3 = Vec3.ZERO
    var vertex4: Vec3 = Vec3.ZERO
    var vertex5: Vec3 = Vec3.ZERO
    var vertex6: Vec3 = Vec3.ZERO
    var vertex7: Vec3 = Vec3.ZERO
    var vertex8: Vec3 = Vec3.ZERO
    lateinit var vertices: Array<Vec3>

    init {
        updateVertex()
    }

    private fun updateVertex(): OrientedBox {
        rotation[0, 0] = axisX.x.toFloat()
        rotation[0, 1] = axisX.y.toFloat()
        rotation[0, 2] = axisX.z.toFloat()
        rotation[1, 0] = axisY.x.toFloat()
        rotation[1, 1] = axisY.y.toFloat()
        rotation[1, 2] = axisY.z.toFloat()
        rotation[2, 0] = axisZ.x.toFloat()
        rotation[2, 1] = axisZ.y.toFloat()
        rotation[2, 2] = axisZ.z.toFloat()

        scaledAxisX = axisX.scale(extent.x)
        scaledAxisY = axisY.scale(extent.y)
        scaledAxisZ = axisZ.scale(extent.z)

        vertex1 = center.subtract(scaledAxisZ).subtract(scaledAxisX).subtract(scaledAxisY)
        vertex2 = center.subtract(scaledAxisZ).add(scaledAxisX).subtract(scaledAxisY)
        vertex3 = center.subtract(scaledAxisZ).add(scaledAxisX).add(scaledAxisY)
        vertex4 = center.subtract(scaledAxisZ).subtract(scaledAxisX).add(scaledAxisY)
        vertex5 = center.add(scaledAxisZ).subtract(scaledAxisX).subtract(scaledAxisY)
        vertex6 = center.add(scaledAxisZ).add(scaledAxisX).subtract(scaledAxisY)
        vertex7 = center.add(scaledAxisZ).add(scaledAxisX).add(scaledAxisY)
        vertex8 = center.add(scaledAxisZ).subtract(scaledAxisX).add(scaledAxisY)

        vertices = arrayOf(
            vertex1,
            vertex2,
            vertex3,
            vertex4,
            vertex5,
            vertex6,
            vertex7,
            vertex8
        )

        return this
    }


    // 1. CONFIGURE
    fun offsetAlongAxisX(offset: Double): OrientedBox {
        this.center = center.add(axisX.scale(offset))
        return this
    }

    fun offsetAlongAxisY(offset: Double): OrientedBox {
        this.center = center.add(axisY.scale(offset))
        return this
    }

    fun offsetAlongAxisZ(offset: Double): OrientedBox {
        this.center = center.add(axisZ.scale(offset))
        return this
    }

    fun offset(offset: Vec3): OrientedBox {
        this.center = center.add(offset)
        return this
    }

    fun scale(scale: Double): OrientedBox {
        this.extent = extent.scale(scale)
        return this
    }


    // 2. CHECK INTERSECTIONS
    fun intersects(boundingBox: AABB?): Boolean {
        val otherOBB = OrientedBox(boundingBox!!).updateVertex()
        return Intersects(this, otherOBB)
    }

    fun intersects(otherOBB: OrientedBox): Boolean {
        return Intersects(this, otherOBB)
    }

    companion object {
        /**
         * 计算给定的 OBB 之间是否存在交集。
         * 分轴定理的实现。
         */
        @JvmStatic
        fun Intersects(a: OrientedBox, b: OrientedBox): Boolean {
            if (separated(a.vertices, b.vertices, a.scaledAxisX)) return false
            if (separated(a.vertices, b.vertices, a.scaledAxisY)) return false
            if (separated(a.vertices, b.vertices, a.scaledAxisZ)) return false

            if (separated(a.vertices, b.vertices, b.scaledAxisX)) return false
            if (separated(a.vertices, b.vertices, b.scaledAxisY)) return false
            if (separated(a.vertices, b.vertices, b.scaledAxisZ)) return false

            if (separated(a.vertices, b.vertices, a.scaledAxisX.cross(b.scaledAxisX))) return false
            if (separated(a.vertices, b.vertices, a.scaledAxisX.cross(b.scaledAxisY))) return false
            if (separated(a.vertices, b.vertices, a.scaledAxisX.cross(b.scaledAxisZ))) return false

            if (separated(a.vertices, b.vertices, a.scaledAxisY.cross(b.scaledAxisX))) return false
            if (separated(a.vertices, b.vertices, a.scaledAxisY.cross(b.scaledAxisY))) return false
            if (separated(a.vertices, b.vertices, a.scaledAxisY.cross(b.scaledAxisZ))) return false

            if (separated(a.vertices, b.vertices, a.scaledAxisZ.cross(b.scaledAxisX))) return false
            if (separated(a.vertices, b.vertices, a.scaledAxisZ.cross(b.scaledAxisY))) return false
            if (separated(a.vertices, b.vertices, a.scaledAxisZ.cross(b.scaledAxisZ))) return false

            return true
        }

        @JvmStatic
        private fun separated(vertsA: Array<Vec3>, vertsB: Array<Vec3>, axis: Vec3): Boolean {
            // Handles the crossProduct product = {0,0,0} case
            if (axis == Vec3.ZERO) return false

            var aMin = Double.POSITIVE_INFINITY
            var aMax = Double.NEGATIVE_INFINITY
            var bMin = Double.POSITIVE_INFINITY
            var bMax = Double.NEGATIVE_INFINITY

            // Define two intervals, a and b. Calculate their min and max values
            for (i in 0..7) {
                val aDist = vertsA[i].dot(axis)
                aMin = min(aDist, aMin)
                aMax = max(aDist, aMax)
                val bDist = vertsB[i].dot(axis)
                bMin = min(bDist, bMin)
                bMax = max(bDist, bMax)
            }

            // One-dimensional intersection test between a and b
            val longSpan = max(aMax, bMax) - min(aMin, bMin)
            val sumSpan = aMax - aMin + bMax - bMin
            return longSpan >= sumSpan // > to treat touching as intersection
        }
    }

}