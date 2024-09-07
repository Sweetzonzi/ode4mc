package cn.solarmoon.spark_core.api.block.crop

import cn.solarmoon.spark_core.api.blockstate.INoLimitAgeState
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.BonemealableBlock
import net.minecraft.world.level.block.BushBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.common.CommonHooks
import kotlin.math.min

abstract class BushCropBlock(properties: Properties = Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH))
    : BushBlock(properties), BonemealableBlock, INoLimitAgeState {

    /**
     * 所有生长收割都会自动根据maxAge调整到合适的状态
     *
     * 甜浆果丛默认为3
     */
    abstract override fun getMaxAge(): Int

    override fun isBonemealSuccess(level: Level, random: RandomSource, pos: BlockPos, state: BlockState): Boolean {
        return true
    }

    public override fun isRandomlyTicking(state: BlockState): Boolean {
        return state.getValue(INoLimitAgeState.AGE) < getMaxAge()
    }

    public override fun randomTick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        val i = state.getValue(INoLimitAgeState.AGE)
        if (i < getMaxAge()
            && level.getRawBrightness(pos.above(), 0) >= 9
            && CommonHooks.canCropGrow(level, pos, state, random.nextInt(5) == 0)
            ) {
            val blockstate = state.setValue(INoLimitAgeState.AGE, i + 1)
            level.setBlock(pos, blockstate, 2)
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(blockstate))
            CommonHooks.fireCropGrowPost(level, pos, state)
        }
    }

    override fun isValidBonemealTarget(level: LevelReader, pos: BlockPos, state: BlockState): Boolean {
        return state.getValue(INoLimitAgeState.AGE) < getMaxAge()
    }

    override fun performBonemeal(level: ServerLevel, random: RandomSource, pos: BlockPos, state: BlockState) {
        val i = min(getMaxAge().toDouble(), (state.getValue(INoLimitAgeState.AGE) + 1).toDouble()).toInt()
        level.setBlock(pos, state.setValue(INoLimitAgeState.AGE, i), 2)
    }

    /**
     * 仅仅造成减速
     */
    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        if (entity is LivingEntity) {
            entity.makeStuckInBlock(state, Vec3(0.8, 0.75, 0.8))
        }
    }

    /**
     * 中键物品，默认和产物一致
     */
    open fun getCloneItemStack(getter: BlockGetter?, pos: BlockPos?, state: BlockState?): ItemStack {
        return harvestResults(null, true).item.defaultInstance
    }

    /**
     * 设置收获产物
     * 默认产率和甜浆果丛一致（2-3个）
     * @param level 可为null，为null时会取消随机生成，使得产物固定为0个，配合后面的boolean可以控制为1个
     * @param oneMoreBaseProduct 为true则基础产物增加1个，原版甜浆果丛也是此逻辑（66%生长时会少摘一个，但这里基本都为true，因为需要100生长度才能摘）
     */
    open fun harvestResults(level: Level?, oneMoreBaseProduct: Boolean): ItemStack {
        var j = 0
        if (level != null) {
            j = 1 + level.random.nextInt(2)
        }
        return ItemStack(getHarvestItem(), j + (if (oneMoreBaseProduct) 1 else 0))
    }

    /**
     * 如果不想改倍率直接改这个
     * 获取收割物
     */
    abstract fun getHarvestItem(): Item

    /**
     * 收获功能
     * 直接抄的甜浆果丛（现在不是了）
     * 区别是需要age到顶才能摘
     */
    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult): InteractionResult {
        val i = state.getValue(INoLimitAgeState.AGE)
        val flag = i == getMaxAge()
        if (!flag) {
            return InteractionResult.PASS
        } else if (i > getMaxAge() - 1) {
            popResource(level, pos, harvestResults(level, true))
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0f, 0.8f + level.random.nextFloat() * 0.4f)
            val blockstate = state.setValue(INoLimitAgeState.AGE, ageAfterHarvest())
            level.setBlock(pos, blockstate, 2)
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, blockstate))
            return InteractionResult.sidedSuccess(level.isClientSide)
        } else {
            return InteractionResult.PASS
        }
    }

    /**
     * @return 控制摘取后作物所返回的age阶段，默认减2段
     */
    open fun ageAfterHarvest(): Int {
        return getMaxAge() - 2
    }

}