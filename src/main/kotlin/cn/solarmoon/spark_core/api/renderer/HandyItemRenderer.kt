package cn.solarmoon.spark_core.api.renderer

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.block.BlockRenderDispatcher
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack

abstract class HandyItemRenderer: BlockEntityWithoutLevelRenderer(Minecraft.getInstance().blockEntityRenderDispatcher, Minecraft.getInstance().entityModels) {//

    protected val blockRenderer: BlockRenderDispatcher = Minecraft.getInstance().blockRenderer
    protected val itemRenderer: ItemRenderer = Minecraft.getInstance().itemRenderer

    abstract override fun renderByItem(
        stack: ItemStack,
        displayContext: ItemDisplayContext,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int
    )

}