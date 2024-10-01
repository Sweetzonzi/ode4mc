package cn.solarmoon.spark_core.api.animation.anim

import net.minecraft.util.Mth
import org.joml.Vector3f
import kotlin.math.min

enum class TransitionType {
    /**
     * 立刻重置模型为默认状态，接着播放指定的动画
     */
    INSTANT,

    /**
     * 将当前动作线性过渡到目标动作
     */
    LINEAR;

    // 目前只有线性插值
    fun lerpRot(progress: Double, rotA: Vector3f, rotB: Vector3f): Vector3f {
        val progress = min(progress, 1.0)
        val x = Mth.rotLerp(progress, rotA.x.toDouble(), rotB.x.toDouble()).toFloat()
        val y = Mth.rotLerp(progress, rotA.y.toDouble(), rotB.y.toDouble()).toFloat()
        val z = Mth.rotLerp(progress, rotA.z.toDouble(), rotB.z.toDouble()).toFloat()
        return Vector3f(x, y, z)
    }

    fun lerpValue(progress: Double, posA: Vector3f, posB: Vector3f): Vector3f {
        val progress = min(progress.toFloat(), 1f)
        val x = Mth.lerp(progress, posA.x, posB.x).toFloat()
        val y = Mth.lerp(progress, posA.y, posB.y).toFloat()
        val z = Mth.lerp(progress, posA.z, posB.z).toFloat()
        return Vector3f(x, y, z)
    }

}