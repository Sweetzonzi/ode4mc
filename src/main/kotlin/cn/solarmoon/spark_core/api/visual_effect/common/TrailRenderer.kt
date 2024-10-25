package cn.solarmoon.aurorian2_bosses_reborn.client.visual_effect

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.visual_effect.common.Trail
import cn.solarmoon.spark_core.api.visual_effect.VisualEffectRenderer
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import java.awt.Color

class TrailRenderer: VisualEffectRenderer() {

    companion object {
        private val TRAIL_RENDER_TYPE = RenderType.create(
            "trail", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true,
            RenderType.CompositeState.builder()
                .setShaderState(RenderType.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(RenderStateShard.TextureStateShard(ResourceLocation.fromNamespaceAndPath(
                    SparkCore.MOD_ID, "textures/visual_effect/trail.png"), true, true))
                .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                .setCullState(RenderType.NO_CULL)
                .setLightmapState(RenderType.LIGHTMAP)
                .setOutputState(RenderType.ITEM_ENTITY_TARGET)
                .createCompositeState(true)
        )

        @JvmStatic
        val trails = arrayListOf<Trail>()
        @JvmStatic
        val removeT = arrayListOf<Trail>()
    }

    override fun render(
        mc: Minecraft,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        camPos: Vec3
    ) {
        val buffer = bufferSource.getBuffer(TRAIL_RENDER_TYPE)
        // 对缓存的拖影进行渲染以及tick，显然，越新的拖影越在列表之后，因此可用当前序列和下一个序列的拖影组成单位长方形，如此混合便可以遍历全部轨迹（也就是分成了微元）
        trails.forEachIndexed { index, trail ->
            // 基本tick，让其流转到生命周期结束
            trail.tick()
            if (trail.isFinished) {
                removeT.add(trail)
            }
            // 四个点组成单位长方形，当然不能忘了要转为相对摄影机的世界坐标
            val p1 = trails[index]; val p2 = if (index + 1 < trails.size) trails[index + 1] else trails[index]
            val pot1s = p1.start.subtract(camPos).toVector3f(); val pot1e = p1.end.subtract(camPos).toVector3f()
            val pot2s = p2.start.subtract(camPos).toVector3f(); val pot2e = p2.end.subtract(camPos).toVector3f()
            // 颜色/亮度/透明度
            val color = Color.red.getRGBComponents(null)
            val light = 15728880 // LevelRenderer.getLightColor(player.clientLevel, player.onPos.above())
            fun p(p: Trail): Float = 1 - 1f * p.progress
            // 法线
            val normal = Vector3f(0f, 1f, 0f).normalize()
            // 渲染顶点
            buffer.addVertex(pot1s.x, pot1s.y, pot1s.z).setColor(color[0], color[1], color[2], p(p1)).setLight(light).setUv(1f, 0f).setOverlay(0).setNormal(normal.x, normal.y, normal.z)
            buffer.addVertex(pot1e.x, pot1e.y, pot1e.z).setColor(color[0], color[1], color[2], p(p1)).setLight(light).setUv(0f, 1f).setOverlay(0).setNormal(normal.x, normal.y, normal.z)
            buffer.addVertex(pot2e.x, pot2e.y, pot2e.z).setColor(color[0], color[1], color[2], p(p2)).setLight(light).setUv(0f, 1f).setOverlay(0).setNormal(normal.x, normal.y, normal.z)
            buffer.addVertex(pot2s.x, pot2s.y, pot2s.z).setColor(color[0], color[1], color[2], p(p2)).setLight(light).setUv(1f, 0f).setOverlay(0).setNormal(normal.x, normal.y, normal.z)
        }
        // 延迟结算，比较高效
        trails.removeAll(removeT)
        removeT.clear()
    }

}