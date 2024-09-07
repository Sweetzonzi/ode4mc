package cn.solarmoon.spark_core.api.blockstate

import net.minecraft.world.level.block.SimpleWaterloggedBlock
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty

interface IWaterLoggedState: SimpleWaterloggedBlock {
    companion object {
        @JvmStatic
        val WATERLOGGED: BooleanProperty = BlockStateProperties.WATERLOGGED
    }
}