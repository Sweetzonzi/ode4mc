package cn.solarmoon.spark_core.api.phys.collision

import cn.solarmoon.spark_core.SparkCore
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import org.jline.utils.Colors
import org.joml.Vector3f
import java.awt.Color

/**
 * 控制box在debug中的渲染
 *
 * **只能在客户端侧运行**
 */
data class FreeCollisionBoxRenderManager(val id: String, val box: FreeCollisionBox) {

    /**
     * 最长显示时间
     */
    var maxTime = (5 / 0.02).toInt()
    var tick = 0
    var color = Color.WHITE

    fun tick() {
        tick++
        if (tick > maxTime) {
            tick = 0
            stop()
        }
    }

    /**
     * 标记已经击中了目标
     *
     * 会改变debug框颜色并刷新显示时间
     */
    fun setHit() {
        color = Color.RED
        tick = 0
    }

    fun start() {
        RENDERABLE_BOXES[id] = this
    }

    fun stop() {
        RENDERABLE_BOXES.remove(id)
    }

    companion object {
        @JvmStatic
        val RENDERABLE_BOXES = mutableMapOf<String, FreeCollisionBoxRenderManager>()
    }

}