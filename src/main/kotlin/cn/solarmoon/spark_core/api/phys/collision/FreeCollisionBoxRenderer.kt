package cn.solarmoon.spark_core.api.phys.collision

import cn.solarmoon.spark_core.SparkCore
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.debug.DebugRenderer
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.jline.utils.Colors
import org.joml.Quaternionf
import org.joml.Vector3f
import java.awt.Color

class FreeCollisionBoxRenderer(private val mc: Minecraft): DebugRenderer.SimpleDebugRenderer {
    override fun render(
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        camX: Double,
        camY: Double,
        camZ: Double
    ) {
        if (!mc.entityRenderDispatcher.shouldRenderHitBoxes()) return
        val buffer = bufferSource.getBuffer(RenderType.LINES)
        FreeCollisionBoxRenderManager.RENDERABLE_BOXES.toMap().forEach { id, it ->
            it.tick()
            val box = it.box
            box.connections.forEach { (c1, c2) ->
                val vs = box.vertexes
                val v1 = vs[c1]
                val v2 = vs[c2]
                val normal = Vector3f(v2.x.toFloat() - v1.x.toFloat(), v2.y.toFloat() - v1.y.toFloat(), v2.z.toFloat() - v1.z.toFloat()).normalize()
                val color = it.color
                buffer.addVertex(poseStack.last().pose(), v1.x.toFloat() - camX.toFloat(), v1.y.toFloat() - camY.toFloat(), v1.z.toFloat() - camZ.toFloat())
                    .setColor(color.red, color.green, color.blue, color.alpha)
                    .setNormal(poseStack.last(), normal.x, normal.y, normal.z) // 法线方向，影响光照，将会随着与光线夹角越小亮度越大
                buffer.addVertex(poseStack.last().pose(), v2.x.toFloat() - camX.toFloat(), v2.y.toFloat() - camY.toFloat(), v2.z.toFloat() - camZ.toFloat())
                    .setColor(color.red, color.green, color.blue, color.alpha)
                    .setNormal(poseStack.last(), normal.x, normal.y, normal.z)
            }
        }
    }
}