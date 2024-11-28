package cn.solarmoon.spark_core.api.visual_effect

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.world.phys.Vec3
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.client.event.ClientTickEvent

abstract class VisualEffectRenderer {

    init {
        ALL_VISUAL_EFFECTS.add(this)
    }

    abstract fun render(mc: Minecraft, camPos: Vec3, poseStack: PoseStack, bufferSource: MultiBufferSource, partialTicks: Float)

    abstract fun tick()

    companion object {
        @JvmStatic
        val ALL_VISUAL_EFFECTS = mutableListOf<VisualEffectRenderer>()
    }

}