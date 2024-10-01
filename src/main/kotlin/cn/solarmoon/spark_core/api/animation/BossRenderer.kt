package cn.solarmoon.spark_core.api.animation

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.anim.ClientAnimData
import cn.solarmoon.spark_core.registry.common.SparkAttachments
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import kotlin.math.PI

class BossRenderer(context: EntityRendererProvider.Context): EntityRenderer<Boss>(context) {

    override fun render(
        boss: Boss,
        entityYaw: Float,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int
    ) {
        super.render(boss, entityYaw, partialTick, poseStack, bufferSource, packedLight)

        val buffer = bufferSource.getBuffer(RenderType.entityTranslucent(getTextureLocation(boss)))

        val animData = boss.animData
        val model = animData.model
        poseStack.pushPose()
        val overlay = if (boss.hurtTime > 0) 0 else OverlayTexture.NO_OVERLAY // 受击变红
        val matrix = boss.getEntityMatrix(partialTick).rotateY(PI.toFloat())
        model.renderBones(animData, matrix, poseStack.last().normal(), buffer, packedLight, overlay, -1, partialTick)
        poseStack.popPose()

    }

    override fun getTextureLocation(entity: Boss): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(SparkCore.MOD_ID, "textures/entity/runestone_dungeon_keeper.png")
    }

}