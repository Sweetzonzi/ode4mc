package cn.solarmoon.spark_core.api.phys.obb.renderable

import cn.solarmoon.spark_core.api.phys.obb.OrientedBoundingBox
import java.awt.Color

/**
 * 在客户端侧对可渲染箱进行操作，如果在服务端无需进行操作，将操作仍在客户端进行，然后通过服务端同步刷新颜色或当前box即可
 */
class RenderableOBB {

    var maxTime: Int = 2
    var defaultColor: Color = Color.WHITE
    var tick = 0
    var colorTick = 0
    var maxColorTick = 5
    var color: Color = defaultColor
        private set
    var box: OrientedBoundingBox? = null
    var lastBox: OrientedBoundingBox? = null
    var isRemoved: Boolean = false

    fun setColor(color: Color) {
        this.color = color
        colorTick = 0
    }

    fun tick() {
        lastBox = this.box
        if (tick >= maxTime) {
            remove()
        } else {
            tick++
        }

        if (color != defaultColor) {
            if (colorTick < maxColorTick) colorTick++
            else color = defaultColor
        }
    }

    fun refresh(box: OrientedBoundingBox) {
        tick = 0
        this.box = box
    }

    fun getBox(partialTicks: Float): OrientedBoundingBox? {
        if (box == null) return null
        if (lastBox == null) return box
        return lastBox!!.lerp(partialTicks, box!!)
    }

    fun remove() {
        isRemoved = true
    }

}