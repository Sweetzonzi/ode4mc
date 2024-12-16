package cn.solarmoon.spark_core.api.phys.obb.renderable

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.phys.lerp
import org.ode4j.ode.DBox
import org.ode4j.ode.DGeom
import java.awt.Color

/**
 * 在客户端侧对可渲染箱进行操作，如果在服务端无需进行操作，将操作仍在客户端进行，然后通过服务端同步刷新颜色或当前box即可
 */
class RenderableOBB {

    var maxTime: Int = 1
    var defaultColor: Color = Color.WHITE
    var tick = 0
    var colorTick = 0
    var maxColorTick = 5
    var color: Color = defaultColor
        private set
    var box: DGeom? = null
    var lastBox: DGeom? = null
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

    /**
     * 注意：刷新最好选用当前动画+speed处的box，因为动画的partialticks是为了补齐从这一tick到下一tick的空缺，而这里的partialtick则是为了补齐上一tick到这一tick的空缺，因此调用当前
     * 动画对应的生成的box必然导致box滞后1tick
     */
    fun refresh(box: DGeom, straight: Boolean = false) {
        tick = 0
        this.box = box
        if (straight) lastBox = box
    }

    fun getBox(partialTicks: Float): DGeom? {
        if (box == null) return null
        if (lastBox == null) return box
        if (lastBox!!.position == box!!.position) return box
        if (lastBox is DBox && box is DBox) {
            return (lastBox as DBox).lerp(partialTicks.toDouble(), box as DBox)
        }
        return box
    }

    fun remove() {
        isRemoved = true
    }

}