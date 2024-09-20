package cn.solarmoon.spark_core.api.util

import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult


object HitResultUtil {

    /**
     * 抄自Item内
     * @return 返回玩家视野击中的方块
     */
    @JvmStatic
    fun getPlayerPOVHitResult(level: Level, player: Player, fluid: ClipContext.Fluid): BlockHitResult {
        val f = player.xRot
        val f1 = player.yRot
        val vec3 = player.eyePosition
        val f2 = Mth.cos(-f1 * (Math.PI.toFloat() / 180f) - Math.PI.toFloat())
        val f3 = Mth.sin(-f1 * (Math.PI.toFloat() / 180f) - Math.PI.toFloat())
        val f4 = -Mth.cos(-f * (Math.PI.toFloat() / 180f))
        val f5 = Mth.sin(-f * (Math.PI.toFloat() / 180f))
        val f6 = f3 * f4
        val f7 = f2 * f4
        val d0: Double = player.blockInteractionRange()
        val vec31 = vec3.add(f6.toDouble() * d0, f5.toDouble() * d0, f7.toDouble() * d0)
        return level.clip(ClipContext(vec3, vec31, ClipContext.Block.OUTLINE, fluid, player))
    }

    /**
     * 抄自Item内
     * @param rayLength 自定义视野长度
     * @return 返回玩家视野击中的方块
     */
    @JvmStatic
    fun getPlayerPOVHitResult(
        level: Level,
        player: Player,
        fluid: ClipContext.Fluid,
        rayLength: Double
    ): BlockHitResult {
        val f = player.xRot
        val f1 = player.yRot
        val vec3 = player.eyePosition
        val f2 = Mth.cos(-f1 * (Math.PI.toFloat() / 180f) - Math.PI.toFloat())
        val f3 = Mth.sin(-f1 * (Math.PI.toFloat() / 180f) - Math.PI.toFloat())
        val f4 = -Mth.cos(-f * (Math.PI.toFloat() / 180f))
        val f5 = Mth.sin(-f * (Math.PI.toFloat() / 180f))
        val f6 = f3 * f4
        val f7 = f2 * f4
        val d0 = rayLength
        val vec31 = vec3.add(f6.toDouble() * d0, f5.toDouble() * d0, f7.toDouble() * d0)
        return level.clip(ClipContext(vec3, vec31, ClipContext.Block.OUTLINE, fluid, player))
    }

}