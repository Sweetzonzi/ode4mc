package cn.solarmoon.spark_core.api.phys

import net.minecraft.world.phys.Vec3
import org.checkerframework.checker.units.qual.kg
import org.joml.Matrix3f
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.TreeMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

fun Vec3.toRadians(): Vec3 {
    return Vec3(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z))
}

fun Vec3.toDegrees(): Vec3 {
    return Vec3(Math.toDegrees(x), Math.toDegrees(y), Math.toDegrees(z))
}

fun Vector3f.toRadians(): Vector3f {
    return Vector3f(Math.toRadians(x.toDouble()).toFloat(), Math.toRadians(y.toDouble()).toFloat(), Math.toRadians(z.toDouble()).toFloat())
}

fun Vector3f.toDegrees(): Vector3f {
    return Vector3f(Math.toDegrees(x.toDouble()).toFloat(), Math.toDegrees(y.toDouble()).toFloat(), Math.toDegrees(z.toDouble()).toFloat())
}

fun Vector3f.copy(): Vector3f = Vector3f(this)

fun Float.toRadians(): Float {
    return Math.toRadians(this.toDouble()).toFloat()
}

fun Float.toDegrees(): Float {
    return Math.toDegrees(this.toDouble()).toFloat()
}

fun Double.toRadians(): Float {
    return Math.toRadians(this).toFloat()
}

fun Quaternionf.getScaledAxisX(): Vector3f {
    val matrix = Matrix3f().rotate(this)
    return Vector3f(matrix.m00, matrix.m01, matrix.m02)
}

fun Quaternionf.getScaledAxisY(): Vector3f {
    val matrix = Matrix3f().rotate(this)
    return Vector3f(matrix.m10, matrix.m11, matrix.m12)
}

fun Quaternionf.getScaledAxisZ(): Vector3f {
    val matrix = Matrix3f().rotate(this)
    return Vector3f(matrix.m20, matrix.m21, matrix.m22)
}

fun Quaternionf.copy(): Quaternionf = Quaternionf(this)