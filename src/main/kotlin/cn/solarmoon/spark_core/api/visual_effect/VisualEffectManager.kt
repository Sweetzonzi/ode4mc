package cn.solarmoon.spark_core.api.visual_effect

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.world.phys.Vec3

object VisualEffectManager {

    /**
     * start会在某一刻添加视觉效果渲染，如果想要连续的视效，往往需要在tick中不断调用start
     */
    @JvmStatic
    fun start(effect: IVisualEffect) {
        effect.addToRenderer()
    }

    @JvmStatic
    val EFFECT_RENDERERS = arrayListOf<VisualEffectRenderer>()

    /**
     * 注册顺序会初步决定渲染顺序，越后面注册的越在渲染上层
     */
    @JvmStatic
    fun registerRenderer(renderer: VisualEffectRenderer): VisualEffectRenderer {
        EFFECT_RENDERERS.add(renderer)
        return renderer
    }

    @JvmStatic
    fun renderAllEffects(mc: Minecraft, poseStack: PoseStack, bufferSource: MultiBufferSource, camPos: Vec3) {
        EFFECT_RENDERERS.forEach {
            if (it.shouldRender) {
                poseStack.pushPose()
                it.render(mc, poseStack, bufferSource, camPos)
                poseStack.popPose()
            }
        }
    }

}