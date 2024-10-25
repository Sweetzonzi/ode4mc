package cn.solarmoon.spark_core.api.visual_effect

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.world.phys.Vec3

abstract class VisualEffectRenderer {

    open val shouldRender get() = true

    abstract fun render(mc: Minecraft, poseStack: PoseStack, bufferSource: MultiBufferSource, camPos: Vec3)

}