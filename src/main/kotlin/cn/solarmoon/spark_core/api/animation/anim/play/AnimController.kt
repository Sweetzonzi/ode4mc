package cn.solarmoon.spark_core.api.animation.anim.play

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.IAnimatable
import cn.solarmoon.spark_core.api.animation.anim.InterpolationType
import cn.solarmoon.spark_core.api.animation.anim.Loop
import net.minecraft.world.entity.Entity

/**
 * 必须注意的是，为了保证能够在服务端使用动画数据，此处内容最好在双端同时进行调用
 *
 * 在客户端中如果要获取当前控制器的数据，就不要调用控制器获取了，使用[AnimData]来获取同步后的数据，这样也在操作上分为两端，防止双端问题
 */
class AnimController<T: IAnimatable<*>>(private val animatable: T) {

    /**
     * 添加动画到现有动画组
     */
    fun addAnimation(vararg mixedAnimation: MixedAnimation) {
        val mixes = animatable.animData.playData.mixedAnimations
        mixes.addAll(mixedAnimation)
    }

    /**
     * 把所有动画加入删除队列并添加动画到动画组
     */
    fun stopAndAddAnimation(vararg mixedAnimation: MixedAnimation) {
        val mixes = animatable.animData.playData.mixedAnimations
        mixes.forEach { it.isCancelled = true }
        mixes.addAll(mixedAnimation)
    }

    /**
     * 停止动画组里和添加的动画所有相同名称的动画并添加动画到组
     */
    fun stopSameAndAddAnimation(vararg mixedAnimation: MixedAnimation) {
        val inAnim = mixedAnimation.map { it.animation.name }
        val mixes = animatable.animData.playData.mixedAnimations
        mixes.forEach {  if (it.animation.name in inAnim) it.isCancelled = true }
        mixes.addAll(mixedAnimation)
    }

    /**
     * 仅当动画组里不存在该动画时才添加
     */
    fun addAnimationWhenNonSame(vararg mixedAnimation: MixedAnimation) {
        val mixes = animatable.animData.playData.mixedAnimations
        val exists = mixes.map { it.animation.name }
        val addList = mixedAnimation.filter { it.animation.name !in exists }
        mixes.addAll(addList)
    }

    /**
     * 停止指定的动画
     */
    fun stop(vararg name: String) {
        name.forEach {
            animatable.animData.playData.getMixedAnimations(it).forEach { it.isCancelled = true }
        }
    }

    /**
     * 动画的tick，默认情况下会自动在接入了IAnimatable的生物的tick中调用此方法
     *
     * 此方法是双端调用的，主要使用的是在客户端模拟预测服务端的操作，单独进行渲染，直到数据差异和服务端过大，则强制接受服务端信息。
     */
    fun animTick() {
        val data = animatable.animData
        val mixedAnimations = data.playData.mixedAnimations

        val removeList = arrayListOf<MixedAnimation>()
        val addList = arrayListOf<MixedAnimation>()
        mixedAnimations.forEach {
            if (it.isCancelled) {
                if (it.transTick > 0) it.transTick--
                else removeList.add(it)
            } else if (it.transTick < it.maxTransTick) {
                it.transTick++
            } else {
                if (it.tick < it.maxTick - if (it.animation.loop == Loop.TRUE) 1 else 0) it.tick++
                else {
                    when(it.animation.loop) {
                        Loop.TRUE -> it.tick = 0.00001
                        Loop.ONCE -> it.isCancelled = true
                        Loop.HOLD_ON_LAST_FRAME -> Unit
                    }
                }
            }
        }
        mixedAnimations.removeAll(removeList)
        mixedAnimations.addAll(addList)
    }

    /**
     * 是否正在播放任意动画
     */
    val isPlayingAnim get() = !isPlaying(null)

    /**
     * 是否正在播放某个动画，对于多个同名动画，只要有一个在播放就为true
     *
     * 检测是否不在动画状态可以输入null
     */
    fun isPlaying(name: String?): Boolean {
        val play = animatable.animData.playData
        if (name == null) return play.mixedAnimations.any { !it.isCancelled }
        return animatable.animData.playData.getMixedAnimations(name).any { it.isCancelled == false }
    }

}