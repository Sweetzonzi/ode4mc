package cn.solarmoon.spark_core.api.util

import net.minecraft.core.Direction
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

object VoxelShapeUtil {//

    /**
     * @return 便捷地根据方向旋转碰撞箱，但要注意这并非改变了输入的shape，因而需要重设shape为该方法返回的值
     */
    @JvmStatic
    fun rotateShape(to: Direction, shape: VoxelShape): VoxelShape {
        val buffer = arrayOf(shape, Shapes.empty())

        val times = (to.get2DDataValue() - Direction.NORTH.get2DDataValue() + 4) % 4
        for (i in 0 until times) {
            buffer[0].forAllBoxes { minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double ->
                buffer[1] = Shapes.or(buffer[1], Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX))
            }
            buffer[0] = buffer[1]
            buffer[1] = Shapes.empty()
        }

        return buffer[0]
    }

}