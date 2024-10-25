package cn.solarmoon.spark_core.api.block.crop

import cn.solarmoon.spark_core.api.blockstate.IMultilayerState
import cn.solarmoon.spark_core.api.blockstate.IMultilayerState.Companion.LAYER
import cn.solarmoon.spark_core.api.blockstate.IWaterLoggedState
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.common.CommonHooks

abstract class MultilayerCropBlock(properties: Properties): HandyCropBlock(properties), IMultilayerState {//

    override fun randomTick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        if (!level.isAreaLoaded(pos, 1)) return  // Forge: prevent loading unloaded chunks when checking neighbor's light
        if (level.getRawBrightness(pos, 0) >= 9) {
            val i = this.getAge(state)
            val f = getGrowthSpeed(state, level, pos)
            if (i < this.maxAge) {
                if (CommonHooks.canCropGrow(level, pos, state, random.nextInt((25.0f / f).toInt() + 1) == 0)) {
                    level.setBlock(pos, state.setValue(AGE, i + 1), 2)
                    CommonHooks.fireCropGrowPost(level, pos, state)
                }
            } else {
                if (CommonHooks.canCropGrow(level, pos, state, random.nextInt((25.0f / f).toInt() + 1) == 0)) {
                    val above = pos.above()
                    val layer = state.getValue(LAYER)
                    // 上方是空气，且不是最大层
                    if (newLayerExtraCondition(state, level, pos, random) && layer < getMaxLayer()) {
                        level.setBlock(above, calibrateState(state.setValue(LAYER, layer + 1).setValue(AGE, 0)), 2)
                        CommonHooks.fireCropGrowPost(level, pos, state)
                    }
                }
            }
        }
    }

    /**
     * @return 生长出新层所需额外条件
     */
    open fun newLayerExtraCondition(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource): Boolean {
        return level.getBlockState(pos.above()).isAir
    }

    override fun growCrops(level: Level, pos: BlockPos, state: BlockState) {
        // 先洒满阶段，阶段满了以后尝试增层
        val layer = state.getValue(LAYER)
        val age = getAge(state)
        if (age < maxAge) {
            var i = this.getAge(state) + this.getBonemealAgeIncrease(level)
            val j = this.maxAge
            if (i > j) {
                i = j
            }
            level.setBlock(pos, state.setValue(AGE, i), 2)
        } else if (layer < getMaxLayer()) {
            level.setBlock(pos.above(), calibrateState(state.setValue(LAYER, layer + 1).setValue<Int, Int>(AGE, 0)), 2)
        }
    }

    /**
     * @return 给需要放置结果的state处进行校准，防止在非原位放置不需要的state
     */
    open fun calibrateState(state: BlockState): BlockState {
        if (state.values[IWaterLoggedState.WATERLOGGED] != null && state.getValue(IWaterLoggedState.WATERLOGGED)) {
            return state.setValue(IWaterLoggedState.WATERLOGGED, false)
        }
        return state
    }

    override fun isValidBonemealTarget(level: LevelReader, pos: BlockPos, state: BlockState): Boolean {
        val layer = state.getValue(LAYER)
        // 骨粉使用条件：
        // 1.不是最大层
        // 2.上层为空
        val canGrow = layer < getMaxLayer() && level.getBlockState(pos.above()).isAir
        return super.isValidBonemealTarget(level, pos, state) || canGrow
    }

    /**
     * @return 特指第一层的生存条件
     */
    open fun canSurviveEachLayer(state: BlockState, levelReader: BlockGetter, pos: BlockPos, layer: Int): Boolean {
        return true
    }

    override fun canSurvive(state: BlockState, levelReader: LevelReader, pos: BlockPos): Boolean {
        val layer = state.getValue(LAYER)
        val stateBelow = levelReader.getBlockState(pos.below())
        val base = levelReader.getRawBrightness(pos, 0) >= 8 || levelReader.canSeeSky(pos)
        // 层数不是最底层，就必须满足下一层是相同类型且连续连接
        if (layer > 0) {
            return base && IMultilayerState.presenceOfConnection(levelReader, pos, pos.below()) && canSurviveEachLayer(state, levelReader, pos, layer)
        }
        // 如果是最底层，那么既要能判别下一格方块的放置许可情况，也要判别自身许可条件
        return base && canSurviveEachLayer(state, levelReader, pos, layer) && mayPlaceOn(stateBelow, levelReader, pos.below())
    }

    override fun isRandomlyTicking(state: BlockState): Boolean {
        return super.isRandomlyTicking(state) || state.getValue(LAYER) < getMaxLayer()
    }

}