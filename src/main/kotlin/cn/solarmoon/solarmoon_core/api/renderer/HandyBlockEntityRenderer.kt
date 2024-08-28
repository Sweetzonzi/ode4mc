package cn.solarmoon.solarmoon_core.api.renderer

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context
import net.minecraft.world.level.block.entity.BlockEntity

abstract class HandyBlockEntityRenderer<B: BlockEntity>(val context: Context): BlockEntityRenderer<B> {



}