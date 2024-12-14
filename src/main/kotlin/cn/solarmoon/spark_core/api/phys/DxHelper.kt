package cn.solarmoon.spark_core.api.phys

import cn.solarmoon.spark_core.SparkCore
import net.minecraft.world.phys.AABB
import org.joml.Vector3d
import org.ode4j.math.DVector3
import org.ode4j.math.DVector3C
import org.ode4j.ode.DAABB
import org.ode4j.ode.DAABBC
import org.ode4j.ode.DBody
import org.ode4j.ode.DBox
import org.ode4j.ode.DContactBuffer
import org.ode4j.ode.DGeom
import org.ode4j.ode.DSpace
import org.ode4j.ode.DWorld
import org.ode4j.ode.OdeHelper
import org.ode4j.ode.internal.DxBody
import org.ode4j.ode.internal.DxGeom
import thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d.toVec3

object DxHelper {

    @JvmStatic
    val ALL_GEOMS = mutableMapOf<Int, DGeom>()

    @JvmStatic
    val ALL_BODYS = mutableMapOf<Int, DBody>()

    @JvmStatic
    fun getGeom(id: Int) = ALL_GEOMS[id]

    @JvmStatic
    fun getBody(id: Int) = ALL_BODYS[id]

    @JvmStatic
    fun createNamedBody(world: DWorld, name: String? = null) = OdeHelper.createBody(world).apply { name?.let { data().name = it } }

}

fun DGeom.data() = data as DGeomData

fun DBody.data() = data as DBodyData

fun DBox.getVertexes(): List<Vector3d> {
    val vertices = MutableList(8) { Vector3d() }
    val halfLengths = lengths.toVector3d().div(2.0)

    // 计算每个顶点的相对位置
    for (i in 0 until 8) {
        val relativePos = Vector3d(
            if (i and 1 == 1) halfLengths.x else -halfLengths.x,
            if (i and 2 == 2) halfLengths.y else -halfLengths.y,
            if (i and 4 == 4) halfLengths.z else -halfLengths.z
        )

        val realPos = DVector3()
        getRelPointPos(relativePos.x, relativePos.y, relativePos.z, realPos)
        vertices[i] = realPos.toVector3d()
    }

    // 按照指定顺序重排顶点
    val orderedVertices = listOf(vertices[0], vertices[2], vertices[4], vertices[6],
        vertices[1], vertices[3], vertices[5], vertices[7])

    return orderedVertices
}

fun AABB.toDAABB() = DAABB(minX, maxX, minY, maxY, minZ, maxZ)

fun DAABBC.toDBox(space: DSpace) = OdeHelper.createBox(space, lengths).apply { position = center }