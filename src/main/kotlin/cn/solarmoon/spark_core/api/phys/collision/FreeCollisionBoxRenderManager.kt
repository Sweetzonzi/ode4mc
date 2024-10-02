package cn.solarmoon.spark_core.api.phys.collision

import net.neoforged.neoforge.network.PacketDistributor
import java.awt.Color

/**
 * 控制box在debug中的渲染
 *
 * **只能在客户端侧运行**
 */
data class FreeCollisionBoxRenderManager(
    val id: String,
    val box: FreeCollisionBox,
    var maxTime: Int = (5 / 0.02).toInt(),
    var color: Color = Color.WHITE
) {

    var tick = 0

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
     *
     * 可在服务端调用，会同步到客户端
     */
    fun setHit(toClient: Boolean = false, lifeTime: Int = maxTime) {
        color = Color.RED
        maxTime = lifeTime
        tick = 0
        if (toClient) {
            PacketDistributor.sendToAllPlayers(FreeCollisionBoxData(id, color.rgb, lifeTime, box))
        }
    }

    fun start() {
        RENDERABLE_BOXES[id] = this
    }

    fun stop() {
        RENDERABLE_BOXES.remove(id)
    }

    /**
     * 向客户端渲染一个具体位置的box
     */
    fun sendRenderableBoxToClient() {
        PacketDistributor.sendToAllPlayers(FreeCollisionBoxData(id, color.rgb, maxTime, box))
    }

    companion object {//
        @JvmStatic
        val RENDERABLE_BOXES = mutableMapOf<String, FreeCollisionBoxRenderManager>()
    }

}