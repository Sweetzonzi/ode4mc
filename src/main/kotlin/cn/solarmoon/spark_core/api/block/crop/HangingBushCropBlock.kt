package cn.solarmoon.spark_core.api.block.crop

import cn.solarmoon.spark_core.api.blockstate.INoLimitAgeState.Companion.AGE
import cn.solarmoon.spark_core.api.util.matcher.BlockMatcher
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent
import net.neoforged.neoforge.common.CommonHooks

abstract class HangingBushCropBlock(properties: Properties = Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH)): BushCropBlock(properties) {//

    /**
     * 设置可放置（种植）的地方
     *
     * 一般情况下请修改canSurviveBlock
     */
    override fun mayPlaceOn(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean {
        return (canSurviveBlock().isBlockEqual(state)
                && (level.getBlockState(pos.below())
            .canBeReplaced() || level.getBlockState(pos.below()).block == this))
    }

    /**
     * 设置可放置（种植）的具体方块（悬挂）
     *
     * 这个和tag选填一个
     */
    abstract fun canSurviveBlock(): BlockMatcher

    /**
     * 设置可存在的地方
     * 默认为橡树树叶下方（用的可放置逻辑）
     */
    override fun canSurvive(state: BlockState, levelReader: LevelReader, pos: BlockPos): Boolean {
        return mayPlaceOn(levelReader.getBlockState(pos.above()), levelReader, pos.above())
    }

    /**
     * 中键物品，默认和产物一致
     */
    override fun getCloneItemStack(getter: BlockGetter?, pos: BlockPos?, state: BlockState?): ItemStack {
        return harvestResults(null, true).item.defaultInstance
    }

    /**
     * 设置收获产物
     * 默认产率1个，低概率2-3个，极低概率4个
     * @param level 可为null，为null时会取消随机生成，使得产物固定为0个，配合后面的boolean可以控制为1个
     * @param oneMoreBaseProduct 一般决定了是否为最终成长阶段
     */
    override fun harvestResults(level: Level?, oneMoreBaseProduct: Boolean): ItemStack {
        var j = 0
        if (level != null) {
            val random = level.random.nextFloat()
            if (random < 0.1) j = 1
            else if (random < 0.01) j = 2
            else if (random < 0.001) j = 3
        }
        return ItemStack(getHarvestItem(), j + (if (oneMoreBaseProduct) 1 else 0))
    }

    /**
     * 如果不想改倍率直接改这个
     * 获取收割物
     */
    abstract override fun getHarvestItem(): Item

    /**
     * 这里返回age0，因为整个摘了
     */
    override fun ageAfterHarvest(): Int {
        return 0
    }

    /**
     * 注意看，这里因为是悬挂作物，而这里生长条件需要上方亮度大于⑨，悬挂作物上方不为空，因此需要改成检测下方亮度
     */
    override fun randomTick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        val i = state.getValue(AGE)
        if (i < getMaxAge()
            && level.getRawBrightness(pos.below(), 0) >= 9
            && CommonHooks.canCropGrow(level, pos, state, random.nextInt(5) == 0)
        ) {
            val blockstate = state.setValue(AGE, i + 1)
            level.setBlock(pos, blockstate, 2)
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(blockstate))
            CommonHooks.fireCropGrowPost(level, pos, state)
        }
    }

}