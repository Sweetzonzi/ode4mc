package cn.solarmoon.spark_core.api.phys.collision

import cn.solarmoon.spark_core.api.animation.model.part.CubePart
import net.minecraft.world.phys.AABB
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d.div

fun AABB.toOBB(): FreeCollisionBox {
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
    return FreeCollisionBox(center, size)
}

fun CubePart.toOBB(matrix4f: Matrix4f = Matrix4f()): FreeCollisionBox {
    val ma = Matrix4f(matrix4f)
    ma.translate(pivot.toVector3f())
    ma.rotateZYX(rotation.toVector3f())
    ma.translate(pivot.div(-1.0).toVector3f())
    val vector3f2 = ma.transformPosition(originPos.toVector3f().add(size.toVector3f().div(2f)))
    return FreeCollisionBox(vector3f2, size.toVector3f(), Quaternionf().setFromUnnormalized(ma))
}