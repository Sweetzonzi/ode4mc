package cn.solarmoon.spark_core.api.animation.anim

import cn.solarmoon.spark_core.api.animation.anim.part.Animation

/**
 * 必须注意的是，为了保证能够在服务端使用动画数据，此处内容只能在服务端侧进行修改，然后使用[IAnimatable.syncAnimDataToClient]来同步到客户端
 *
 * 因此，在客户端中如果要获取当前控制器的数据，就不要调用控制器获取了，使用ClientAnimData来获取同步后的数据，这样也在操作上分为两端，防止双端问题
 */
class AnimController<T: IAnimatable<*>>(private val entity: T) {

    var endFunction: () -> Unit = {}

    /**
     * 播放指定动画，可用animationSet的getAnimation方法传入要播放的动画数据
     * @param transTime 过渡到默认动画的时间
     */
    fun start(
        anim: Animation,
        transTime: Int = 5,
        resetPresentAnim: Boolean = false,
        type: TransitionType = TransitionType.LINEAR,
        lifeTime: Double = anim.baseLifeTime,
        endFunction: () -> Unit = {}
    ) {
        // 根据需求决定是否强行设置当前动画
        if (!resetPresentAnim && isPlaying(anim.name)) return
        // 然后切换当前所要过渡到的目标动画
        entity.animData.transTick = 0
        entity.animData.lifeTime = lifeTime
        entity.animData.transitionType = type
        if (transTime == 0) {
            entity.animData.tick = 0
            entity.animData.presentAnim = anim
        } else {
            entity.animData.targetAnim = anim
            entity.animData.maxTransTick = transTime
        }
        this.endFunction = endFunction
        entity.syncAnimDataToClient(true)
    }

    /**
     * 停止当前的动画，回到默认动画
     * @param transTime 过渡到默认动画的时间
     */
    fun stop(transTime: Int = 5, type: TransitionType = TransitionType.LINEAR, lifeTime: Double = entity.animData.defaultAnim?.baseLifeTime ?: 0.0) {
        entity.animData.targetAnim = entity.animData.defaultAnim // 回到默认动画
        entity.animData.transTick = 0
        entity.animData.transitionType = type
        entity.animData.maxTransTick = transTime
        entity.animData.lifeTime = lifeTime
        entity.syncAnimDataToClient(true)
    }

    /**
     * 动画的tick，默认情况下会自动在接入了IAnimatable的生物的tick中调用此方法
     *
     * 此方法是双端调用的，但是对于动画的操作只在服务端有效
     *
     * 关于这里是如何同步动画到客户端的，主要使用的是服务端动画中在客户端模拟预测服务端的操作，单独进行渲染，直到数据差异和服务端过大，则强制接受服务端信息。
     * 这个方法的优点是能够在客户端较为流畅的展示动画，缺点则是受发送延迟的影响会导致服务端的逻辑“看起来”是滞后于客户端的。
     * 为了减缓这种滞后，目前采用的是粗暴的延迟固定时间才让客户端执行动画预测。
     */
    fun animTick() {
        val data = entity.animData

        // 当前动画计时器
        data.presentAnim?.let {
            if (data.targetAnim == null) {
                if (data.tick < data.maxTick) {
                    data.tick++
                }
                else {
                    data.tick = 0
                    // 应用结束指令
                    endFunction.invoke()
                    endFunction = {}
                }
            }
        }

        // 动画过渡计时器
        data.targetAnim?.let {
            if (data.transTick < data.maxTransTick) {
                data.transTick++
            } else {
                // 结束过渡后，位置刚好是目标动画的开始位置，直接设置动画时间等参数
                data.transTick = 0
                data.tick = 0
                data.presentAnim = data.targetAnim
                data.targetAnim = null
            }
        }

        // 初始化默认动画
        data.defaultAnim?.let {
            if (isPlaying(null)) start(it)
        }

        // 常规同步
        entity.syncAnimDataToClient()

    }

    val isPlayingDefaultAnim get() = isPlaying(entity.animData.defaultAnim?.name)

    /**
     * 是否正在播放某个动画，检测是否不在动画状态可以输入null
     *
     * 这里包括了targetAnim，过渡过程中实际上就是要播放当前动画了，所以也算
     */
    fun isPlaying(name: String?): Boolean {
        val presentMatch = entity.animData.presentAnim?.name == name
        val targetMatch = entity.animData.targetAnim?.name == name
        return if (name == null) presentMatch && targetMatch else presentMatch || targetMatch
    }

}