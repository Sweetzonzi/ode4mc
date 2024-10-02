package cn.solarmoon.spark_core.api.animation

import cn.solarmoon.spark_core.api.animation.anim.IAnimatable
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import org.joml.Matrix4f

open class GeoEntityRenderer<T>(context: EntityRendererProvider.Context): EntityRenderer<T>(context) where T : Entity, T : IAnimatable<*> {

    override fun render(
        entity: T,
        entityYaw: Float,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int
    ) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight)
        val buffer = bufferSource.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)))
        val animData = entity.animData
        val model = animData.model
        poseStack.pushPose()
        val overlay = if (entity is LivingEntity && entity.hurtTime > 0) 0 else OverlayTexture.NO_OVERLAY // 受击变红
        val matrix = entity.getEntityMatrix(partialTick)
        beforeRender(entity, entityYaw, matrix, partialTick, poseStack, bufferSource, packedLight)
        model.renderBones(animData, matrix, poseStack.last().normal(), entity.getHeadMatrix(partialTick), buffer, packedLight, overlay, getColor(entity), partialTick)
        poseStack.popPose()
    }

    /**
     * 可在渲染之前调整一些额外的变换
     */
    open fun beforeRender(entity: T, entityYaw: Float, matrix: Matrix4f, partialTick: Float, poseStack: PoseStack, bufferSource: MultiBufferSource, packedLight: Int) {}
    
    open fun getColor(entity: T): Int = -1

    override fun getTextureLocation(entity: T): ResourceLocation {
        val id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.type)
        return ResourceLocation.fromNamespaceAndPath(id.namespace, "textures/entity/${id.path}.png")
    }

}