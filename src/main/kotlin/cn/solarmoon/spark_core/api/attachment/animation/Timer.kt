package cn.solarmoon.spark_core.api.attachment.animation

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

data class Timer(
    var isTiming: Boolean = false,
    var time: Float = 0f,
    var maxTime: Float = 100f
) {

    var onStop = {}

    fun start() {
        isTiming = true
        time = 0f
    }

    fun stop() {
        isTiming = false
    }

    /**
     * 当计时器启用时每tick增加指定计时器值直到最大值停止
     */
    fun tick() {
        if (isTiming) {
            if (time < maxTime) {
                time++
            } else {
                onStop.invoke()
                stop()
            }
        }
    }

    /**
     * 获取当前进度
     */
    fun getProgress(): Float = getProgress(0f)

    /**
     * 当前进度，但是输入了一个值（一般是帧时间）作为过渡，更为平滑
     */
    fun getProgress(partialTicks: Float): Float = (time + partialTicks) / maxTime

    companion object {
        @JvmStatic
        val CODEC: Codec<Timer> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BOOL.fieldOf("isTiming").forGetter { it.isTiming },
                Codec.FLOAT.fieldOf("time").forGetter { it.time },
                Codec.FLOAT.fieldOf("maxTime").forGetter { it.maxTime }
            ).apply(instance, ::Timer)
        }
    }

}