package cn.solarmoon.spark_core.api.visual_effect.common

import cn.solarmoon.spark_core.api.visual_effect.VisualEffectRenderer
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.world.phys.Vec3

class StreakRenderer: VisualEffectRenderer() {

    companion object {
        @JvmStatic
        val streaks = arrayListOf<Streak>()
        @JvmStatic
        val removeS = arrayListOf<Streak>()
    }

    override fun render(
        mc: Minecraft,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        camPos: Vec3
    ) {
        val br = mc.entityRenderDispatcher
        val partialTicks = mc.timer.getGameTimeDeltaPartialTick(true)
        val level = mc.level ?: return

        // 对存在的残影进行渲染，直到残影到达最大存留时间
        streaks.forEach { streak ->
            val entity = level.getEntity(streak.entityId) ?: return@forEach
            val pos = streak.position
            val light = LevelRenderer.getLightColor(level, entity.onPos.above())
            streak.tick()
            if (!streak.isValidToShow) return@forEach
            RenderSystem.enableBlend()
            val originalColors = RenderSystem.getShaderColor().clone()
            val color = streak.color.getRGBComponents(null)
            RenderSystem.setShaderColor(color[0], color[1], color[2], color[3] * (1 - streak.progress))
            br.render(entity, pos.x - camPos.x, pos.y - camPos.y, pos.z - camPos.z, entity.yRot, partialTicks, poseStack, bufferSource, light)
            RenderSystem.setShaderColor(originalColors[0], originalColors[1], originalColors[2], originalColors[3])
            RenderSystem.disableBlend()
            if (streak.isFinished) {
                removeS.add(streak)
            }
        }

        // 结算（删除过时残影）
        streaks.removeAll(removeS)
        removeS.clear()
    }

}