package cn.solarmoon.spirit_of_fight.feature.lock_on

import cn.solarmoon.spark_core.api.util.RenderTypeUtil
import cn.solarmoon.spark_core.api.visual_effect.VisualEffectRenderer
import cn.solarmoon.spirit_of_fight.feature.fight_skill.spirit.FightSpiritGui
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.phys.Vec3
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

class LockOnRenderer: VisualEffectRenderer() {

    override fun tick() {}

    override fun render(
        mc: Minecraft,
        camPos: Vec3,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        partialTicks: Float
    ) {
        LockOnController.target?.let {
            val center = it.boundingBox.center
            val targetPos = center.add(0.0, it.boundingBox.ysize / 2 + 0.2, 0.0)
            mc.level?.addParticle(ParticleTypes.END_ROD, targetPos.x, targetPos.y, targetPos.z, 0.0, 0.0, 0.0)
        }
    }

}