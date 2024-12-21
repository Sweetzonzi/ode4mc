package cn.solarmoon.spark_core.api.phys

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.registry.common.SparkVisualEffects
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.ItemEntityRenderer
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.resources.ResourceLocation

abstract class DxEntityRenderer<T: DxEntity> private constructor( context: EntityRendererProvider.Context): EntityRenderer<T>(context) {

    override fun render(
        entity: T,
        entityYaw: Float,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int
    ) {
        var i = 0
        if (!entity.body.isEnabled) return
        entity.body.geomIterator.forEach {
            //为实体的body上每一个附着的geom创建/选中id为${entity.id}:box-$i的RenderableOBB，并将geom传入其中的缓存区
            SparkVisualEffects.OBB.getRenderableBox("${entity.id}:box-$i").update(it, false)
            i++
            if (entity is DxAnimAttackEntity)
                println(it.position.toVector3d())
        }
    }

    override fun getTextureLocation(entity: T): ResourceLocation {
        return TextureAtlas.LOCATION_BLOCKS
    }

    companion object {
        @JvmStatic
        fun <D: DxEntity> create(context: EntityRendererProvider.Context) = object : DxEntityRenderer<D>(context) {}
    }

}