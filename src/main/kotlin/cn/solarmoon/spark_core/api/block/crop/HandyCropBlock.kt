package cn.solarmoon.spark_core.api.block.crop

import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.CropBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

abstract class HandyCropBlock(properties: Properties = Properties.ofFullCopy(Blocks.WHEAT)): CropBlock(properties) {//

    abstract override fun getBaseSeedId(): ItemLike

    abstract override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape

}