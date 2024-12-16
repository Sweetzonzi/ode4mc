package cn.solarmoon.spark_core.api.animation.anim.play

import cn.solarmoon.spark_core.api.animation.IAnimatable
import cn.solarmoon.spark_core.api.animation.anim.part.Loop
import cn.solarmoon.spark_core.api.animation.sync.AnimFreezingPayload
import net.minecraft.world.entity.Entity
import net.neoforged.neoforge.network.PacketDistributor
import kotlin.collections.contains

/**
 * ### 动画控制器
 * > 动画控制器是动画数据的总控中心，可以通过它来调配动画的播放与暂停，以及其它如动画冻结等对动画的控制功能。
 */
class AnimController<T: IAnimatable<*>>(private val animatable: T) {

    var freezeTick = 0
    var maxFreezeTick = 2
    var freezeSpeedPercent = 1f

    fun modifyAnims(modifier: (MutableSet<MixedAnimation>) -> Unit) {
        animatable.animData.playData.modifyAnims(modifier)
    }

    /**
     * 把所有动画加入删除队列并添加新动画到动画组
     * @param filter 会对每一个正在组里的动画进行遍历，可根据它们的属性选择一个boolean值来决定是否暂停它们
     */
    fun stopAndAddAnimation(vararg mixedAnimation: MixedAnimation, filter: (MixedAnimation) -> Boolean = { true }) {
        modifyAnims {
            val minTransSpeed = mixedAnimation.minOfOrNull { it.startTransSpeed }
            it.forEach {
                if (filter.invoke(it)) {
                    if (minTransSpeed != null) it.endTransSpeed = minTransSpeed
                    it.isCancelled = true
                }
            }
            it.addAll(mixedAnimation)
        }
    }

    fun stopAnimation(vararg name: String) {
        modifyAnims {
            it.forEach { if (it.name in name) it.isCancelled = true }
        }
    }

    fun stopAllAnimation(filter: (MixedAnimation) -> Boolean = { true }) {
        modifyAnims {
            it.filter(filter).forEach { it.isCancelled = true }
        }
    }

    /**
     * 是否正在播放某个动画，对于多个同名动画，只要有一个在播放就为true
     *
     * 检测是否不在动画状态可以输入null
     * @param filter 额外的过滤条件，因为默认情况动画只要存在列表就会视为正在播放，但是可以通过过滤过滤到一些诸如已被设定为删除的动画
     */
    fun isPlaying(name: String?, filter: (MixedAnimation) -> Boolean = { true }): Boolean {
        return isPlaying(name, 0, filter)
    }

    fun isPlaying(name: String?, level: Int, filter: (MixedAnimation) -> Boolean = { true }): Boolean {
        val play = animatable.animData.playData
        if (name == null) return play.getSafeMixedAnims().isEmpty()
        val anim = play.getMixedAnimation(name, level, filter)
        return anim != null
    }

    fun startFreezing(syncToClient: Boolean, speedPercent: Float = 0.05f, freezeTime: Int = 2) {
        modifyAnims {
            freezeTick = freezeTime
            maxFreezeTick = freezeTime
            freezeSpeedPercent = speedPercent

            val entity = animatable.animatable
            if (syncToClient && entity is Entity) {
                PacketDistributor.sendToAllPlayers(AnimFreezingPayload(entity.id, speedPercent, freezeTime))
            }
        }
    }

    var animsCache = setOf<MixedAnimation>()

    fun updateCache() {
        animsCache = animatable.animData.playData.getSafeMixedAnims().map { it.copy() }.toSet()
    }

    /**
     * 动画的tick，默认情况下会自动在接入了IAnimatable的生物的tick中调用此方法
     *
     * 此方法是双端调用的，主要使用的是在客户端模拟预测服务端的操作，单独进行渲染
     */
    fun physTick() {
        val factor = 2f / 5
        animsCache.forEach {
            if (it.shouldBackwardTransition) {
                it.transTick -= it.endTransSpeed * factor
            } else if (it.shouldForwardTransition) {
                it.transTick += it.startTransSpeed * factor
            } else {
                if (it.tick < it.maxTick - if (it.animation.loop == Loop.TRUE) it.speed else 0f) it.tick += it.speed * factor
                else {
                    when (it.animation.loop) {
                        Loop.TRUE -> it.tick = 0.00001
                        Loop.ONCE -> it.isCancelled = true
                        Loop.HOLD_ON_LAST_FRAME -> Unit
                    }
                }
            }
            it.freeze = freezeSpeedPercent
        }
    }

    fun tick() {
        // 播放
        animatable.animData.playData.modifyAnims {
            it.forEach {
                if (it.shouldBackwardTransition) {
                    it.transTick -= it.endTransSpeed
                } else if (it.shouldForwardTransition) {
                    it.transTick += it.startTransSpeed
                } else {
                    if (it.tick < it.maxTick - if (it.animation.loop == Loop.TRUE) it.speed else 0f) it.tick += it.speed
                    else {
                        when (it.animation.loop) {
                            Loop.TRUE -> it.tick = 0.00001
                            Loop.ONCE -> it.isCancelled = true
                            Loop.HOLD_ON_LAST_FRAME -> Unit
                        }
                    }
                }
                it.freeze = freezeSpeedPercent
            }
            it.removeIf { it.isCancelled && it.transTick <= 0 }
        }

        // 冻结（模拟打肉的顿感）
        if (freezeTick > 0) freezeTick--
        else {
            freezeTick = 0
            freezeSpeedPercent = 1f
        }

        updateCache()
    }

}