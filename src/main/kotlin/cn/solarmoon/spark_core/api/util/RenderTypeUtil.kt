package cn.solarmoon.spark_core.api.util

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation

object RenderTypeUtil {

    @JvmStatic
    fun transparentRepair(location: ResourceLocation): RenderType = RenderType.create(
        "transparent_repair_entity", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true,
        RenderType.CompositeState.builder()
            .setShaderState(RenderType.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
            .setTextureState(RenderStateShard.TextureStateShard(location, false, true))
            .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
            .setCullState(RenderType.NO_CULL)
            .setLightmapState(RenderType.LIGHTMAP)
            .setOutputState(RenderType.ITEM_ENTITY_TARGET)
            .createCompositeState(true)
    )

}