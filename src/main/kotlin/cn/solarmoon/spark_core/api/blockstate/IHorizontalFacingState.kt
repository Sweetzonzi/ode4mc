package cn.solarmoon.spark_core.api.blockstate

import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.DirectionProperty

/**
 * 接入后自动为方块添加水平朝向属性（真正的全自动）<br/>
 * 但是要注意，原版中有的方块在用createBlockStateDefinition时没有使用super来继承block的此方法，如果继承了这类的方块，则不得不自己添加一遍。
 */
interface IHorizontalFacingState {//
    companion object {
        @JvmStatic
        val FACING: DirectionProperty = BlockStateProperties.HORIZONTAL_FACING
    }
}