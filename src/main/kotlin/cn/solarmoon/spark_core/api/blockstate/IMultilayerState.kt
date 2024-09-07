package cn.solarmoon.spark_core.api.blockstate

import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.properties.IntegerProperty

interface IMultilayerState {

    fun getMaxLayer(): Int

    companion object {

        @JvmStatic
        val LAYER: IntegerProperty = IntegerProperty.create("layer", 0, 64)

        /**
         * @param pos 当前层的坐标
         * @param connection 检测层的坐标
         * @return 检测层是否有层数，且层数满足当前层和检测层y轴之差（也就是层数是否连续），且方块类型一致
         */
        @JvmStatic
        fun presenceOfConnection(level: Level, pos: BlockPos, connection: BlockPos): Boolean {
            val deltaY = connection.y - pos.y
            val th = level.getBlockState(pos)
            val tt = level.getBlockState(connection)
            if (tt.values[LAYER] == null) return false
            val layerTh = th.getValue(LAYER)
            val layerTT = tt.getValue(LAYER)
            return layerTT - layerTh == deltaY && th.`is`(tt.block)
        }

        /**
         * @param pos 当前层的坐标
         * @param connection 检测层的坐标
         * @return 检测层是否有层数，且层数满足当前层和检测层y轴之差（也就是层数是否连续）,且方块类型一致
         */
        @JvmStatic
        fun presenceOfConnection(level: BlockGetter, pos: BlockPos, connection: BlockPos): Boolean {
            val deltaY = connection.y - pos.y
            val th = level.getBlockState(pos)
            val tt = level.getBlockState(connection)
            if (tt.values[LAYER] == null) return false
            val layerTh = th.getValue(LAYER)
            val layerTT = tt.getValue(LAYER)
            return layerTT - layerTh == deltaY && th.`is`(tt.block)
        }

    }

}