package cn.solarmoon.solarmoon_core.api.util

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.core.Direction

object PoseStackUtil {

    @JvmStatic
    fun rotateByDirection(direction: Direction, poseStack: PoseStack) {
        poseStack.translate(0.5f, 0f, 0.5f)
        poseStack.mulPose(Axis.YN.rotationDegrees(direction.toYRot() - 180))
        poseStack.translate(-0.5f, 0f, -0.5f)
    }

}