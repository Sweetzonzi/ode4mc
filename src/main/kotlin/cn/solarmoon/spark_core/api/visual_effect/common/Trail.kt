package cn.solarmoon.spark_core.api.visual_effect.common

import cn.solarmoon.aurorian2_bosses_reborn.client.visual_effect.TrailRenderer
import cn.solarmoon.spark_core.api.visual_effect.IVisualEffect
import net.minecraft.world.phys.Vec3
import kotlin.math.min

/**
 * 拖影是由多个微方形积分后组成的曲面，代码中每条拖影可看作一条条线段
 * @param start 拖影的第一个端点
 * @param end 拖影的第二个端点
 * @param maxTick 拖影的最大生命周期
 * @param tick 拖影的计时器，默认从0开始
 * 想要获得连续的曲面，只要保证端点为动态值即可
 */
class Trail(
    val start: Vec3,
    val end: Vec3,
    val maxTick: Int,
    var tick: Int = 0
): IVisualEffect {

    val isFinished get() = tick > maxTick
    val progress get() = min(1f, tick.toFloat() / maxTick.toFloat())

    fun tick() {
        if (isFinished) return
        tick += 1
    }

    override fun addToRenderer() {
        TrailRenderer.trails.add(this)
    }

}