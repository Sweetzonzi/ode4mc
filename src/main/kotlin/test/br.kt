package test

import cn.solarmoon.spark_core.api.attachment.animation.AnimHelper
import cn.solarmoon.spark_core.api.renderer.HandyBlockEntityRenderer
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.Direction

class br(context: BlockEntityRendererProvider.Context): HandyBlockEntityRenderer<be>(context) {
    override fun render(
        blockEntity: be,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int
    ) {
        AnimHelper.Fluid.renderAnimatedFluid(blockEntity, Direction.DOWN, 1f, 1f, 1f, partialTick, poseStack, bufferSource, packedLight)
    }
}