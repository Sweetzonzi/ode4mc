package cn.solarmoon.spark_core.api.attachment.counting

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos

class CountingDevice {

    var count: Int = 0
        private set
    var pos: BlockPos = BlockPos.ZERO
    var tick = 0
    var refreshTick: Int = 5

    /**
     * 直接设定数目
     */
    fun setCount(count: Int) {
        this.count = count
        tick = 0
    }

    /**
     * 设定匹配指定坐标的数目<br/>
     * 如果不匹配坐标，就会重置count，并输入新坐标
     */
    fun setCount(count: Int, pos: BlockPos) {
        if (pos == this.pos) {
            this.count = count
        } else {
            this.pos = pos
            this.count = 1 // 重置计数，但不是重置为0，因为pos只在调用此方法时才会做检查并修改，实际上已经右键了一次方块，这里相当于加上右键的第一次
        }
        tick = 0
    }

    /**
     * 当游戏中经过5tick时重置数量为0<br></br>
     * 但在此期间setCount会重置时间计数
     */
    fun tick() {
        if (count == 0) return
        tick++
        if (tick > refreshTick) {
            tick = 0
            count = 0
        }
    }

    companion object {
        @JvmStatic
        val CODEC: Codec<CountingDevice> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.fieldOf("count").forGetter { it.count },
                BlockPos.CODEC.fieldOf("pos").forGetter { it.pos },
                Codec.INT.fieldOf("tick").forGetter { it.tick },
                Codec.INT.fieldOf("refreshTick").forGetter { it.refreshTick }
            ).apply(instance) { count, pos, tick, maxTick ->
                CountingDevice().apply {
                    this.count = count
                    this.pos = pos
                    this.tick = tick
                    this.refreshTick = maxTick
                }
            }
        }
    }

}