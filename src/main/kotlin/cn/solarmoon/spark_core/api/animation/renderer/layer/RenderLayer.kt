package cn.solarmoon.spark_core.api.animation.renderer.layer

import cn.solarmoon.spark_core.api.animation.IAnimatable
import cn.solarmoon.spark_core.api.animation.renderer.IGeoRenderer
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.neoforged.neoforge.attachment.IAttachmentHolder

/**
 * 附加渲染
 */
abstract class RenderLayer<T: IAttachmentHolder>(protected val renderer: IGeoRenderer<T>) {

    abstract fun getTextureLocation(sth: IAnimatable<T>): ResourceLocation

    abstract fun getRenderType(sth: IAnimatable<T>): RenderType

    abstract fun render(sth: IAnimatable<T>, partialTick: Float, poseStack: PoseStack, bufferSource: MultiBufferSource, packedLight: Int, packedOverlay: Int)

}