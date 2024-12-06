package cn.solarmoon.spark_core.api.phys.obb

import cn.solarmoon.spark_core.api.animation.model.part.CubePart
import cn.solarmoon.spark_core.registry.common.SparkAttachments
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.AABB
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d.div

fun AABB.toOBB(): OrientedBoundingBox {
    val center = Vector3f(
        (minX + maxX).toFloat() / 2,
        (minY + maxY).toFloat() / 2,
        (minZ + maxZ).toFloat() / 2
    )
    val size = Vector3f(
        (maxX - minX).toFloat(),
        (maxY - minY).toFloat(),
        (maxZ - minZ).toFloat()
    )
    return OrientedBoundingBox(center, size)
}

fun CubePart.toOBB(matrix4f: Matrix4f = Matrix4f()): OrientedBoundingBox {
    val ma = Matrix4f(matrix4f)
    ma.translate(pivot.toVector3f())
    ma.rotateZYX(rotation.toVector3f())
    ma.translate(pivot.div(-1.0).toVector3f())
    val vector3f2 = ma.transformPosition(originPos.toVector3f().add(size.toVector3f().div(2f)))
    return OrientedBoundingBox(vector3f2, size.toVector3f(), Quaternionf().setFromUnnormalized(ma))
}

/**
 * 保存obb缓存到生物身上
 * > 缓存有什么用？见：[OrientedBoundingBox.connectionIntersects]
 * @param id obb以string标识进行保存，通过自定的id来调用具体的缓存box
 */
fun Entity.popBox(id: String, box: OrientedBoundingBox?) {
    if (box != null) {
        getData(SparkAttachments.OBB_CACHE)[id] = box
    } else {
        getData(SparkAttachments.OBB_CACHE).remove(id)
    }
}

/**
 * 获取指定id的box缓存
 * > 缓存有什么用？见：[OrientedBoundingBox.connectionIntersects]
 * @param id obb以string标识进行保存，通过自定的id来调用具体的缓存box
 */
fun Entity.pushBox(id: String): OrientedBoundingBox? {
    return getData(SparkAttachments.OBB_CACHE)[id]
}