package cn.solarmoon.spark_core.api.visual_effect.common

import cn.solarmoon.spark_core.api.visual_effect.IVisualEffect
import net.minecraft.world.phys.Vec3
import java.awt.Color
import kotlin.math.min

/**
 * @param entityId 残影所渲染的生物的id标识
 * @param position 残影出现的坐标
 * @param maxTick 残影最大持续时间
 * @param color 残影颜色
 * @param startDelay 使得残影出现的延迟时间
 * @param tick 残影的计时器，默认从0开始
 */
data class Streak(
    val entityId: Int,
    val position: Vec3,
    val maxTick: Int,
    val color: Color,
    var startDelay: Int = 0,
    var tick: Int = 0,
): IVisualEffect {

    val isFinished get() = tick > maxTick
    val isValidToShow get() = startDelay <= 0
    val isTicking get() = tick > 0
    val progress get() = min(1f, tick.toFloat() / maxTick.toFloat())

    fun tick() {
        if (tick > maxTick) return
        if (isValidToShow) tick += 1
        else startDelay -= 1
    }

    override fun addToRenderer() {
        StreakRenderer.Companion.streaks.add(this)
    }

}