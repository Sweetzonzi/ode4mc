package cn.solarmoon.spark_core.api.blockstate

import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty

interface INoLimitAgeState {
//
    fun getMaxAge(): Int

    companion object {
        @JvmStatic
        val AGE: IntegerProperty = BlockStateProperties.AGE_25
    }

}