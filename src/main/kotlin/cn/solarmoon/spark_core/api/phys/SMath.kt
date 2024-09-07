package cn.solarmoon.spark_core.api.phys

import kotlin.math.cos
import kotlin.math.pow

object SMath {

    /**
     * 二次差值
     * @param P1 控制点，将决定曲线的过渡重心，比如从0-10中，p1为1，则过渡先慢后快
     */
    fun quadraticInterpolation(t: Float, P0: Float, P1: Float, P2: Float): Float {
        return (1 - t) * (1 - t) * P0 + (2 * (1 - t) * t * P1) + (t * t * P2)
    }

    /**
     * 平滑差值
     * @param n 平滑度（指数）
     */
    fun smoothInterpolation(progress: Float, start: Float, end: Float, n: Float): Float {
        val clampedProgress = progress.coerceIn(0f, 1f) // 将 progress 限制在 0 到 1 之间
        val t = 1 - (1 - clampedProgress).pow(n)
        return start + t * (end - start)
    }

    /**
     * 正弦差值
     */
    fun sineInterpolation(t: Float, P0: Float, P1: Float): Float {
        return P0 + (P1 - P0) * (1 - cos(t * Math.PI).toFloat()) / 2
    }

    /**
     * 余弦差值
     */
    fun cosineInterpolation(t: Float, P0: Float, P1: Float): Float {
        val ft = (t * Math.PI).toFloat()
        val f = (1 - cos(ft.toDouble()).toFloat()) * 0.5f
        return P0 * (1 - f) + P1 * f
    }

    /**
     * 抛物线函数
     * @param x 当前x值（当前点所在抛物线的位置）
     * @param vertexX 顶点x坐标
     * @param vertexY 顶点y坐标
     * @param initialY 初始y坐标
     * @return 当前x值所对应点的y值
     */
    fun parabolaFunction(x: Double, vertexX: Double, vertexY: Double, initialY: Double): Double {
        // a 是抛物线的开口方向和宽度，由顶点和初始值决定
        val a: Double = (initialY - vertexY) / (0 - vertexX).pow(2.0)
        // 使用抛物线方程 y = a*(x-h)^2 + k
        return a * (x - vertexX).pow(2.0) + vertexY
    }

}