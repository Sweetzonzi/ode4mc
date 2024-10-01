package cn.solarmoon.spark_core.api.animation.anim

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.anim.part.Animation
import cn.solarmoon.spark_core.api.animation.anim.part.BoneAnim
import cn.solarmoon.spark_core.api.animation.model.CommonModel
import kotlin.math.ceil

/**
 * 必须注意的是，为了保证能够在服务端使用动画数据，此处内容只能在服务端侧进行修改，然后使用[IAnimatable.saveAndSyncAnimToClient]来同步到客户端
 *
 * 因此，在客户端中如果要获取当前控制器的数据，就不要调用控制器获取了，使用ClientAnimData来获取同步后的数据，这样也在操作上分为两端，防止双端问题
 */
class AnimController<T: IAnimatable<*>>(private val entity: T) {

    /**
     * 播放指定动画，可用animationSet的getAnimation方法传入要播放的动画数据
     * @param transTime 过渡到默认动画的时间
     */
    fun start(anim: Animation, transTime: Int = 5, type: TransitionType = TransitionType.LINEAR, lifeTime: Double = anim.baseLifeTime) {
        // 然后切换当前所要过渡到的目标动画
        entity.animData.targetAnim = anim
        entity.animData.transTick = 0
        entity.animData.lifeTime = lifeTime
        entity.animData.maxTick = ceil(lifeTime * 20).toInt()
        entity.animData.transitionType = type
        entity.animData.maxTransTick = transTime
        entity.syncAnimDataToClient()
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
        entity.syncAnimDataToClient()
    }

    /**
     * 动画的tick，默认情况下会自动在接入了IAnimatable的生物的tick中调用此方法
     *
     * 此方法是双端调用的，但是对于动画的操作只在服务端有效
     */
    fun animTick() {
        val data = entity.animData

        /**
         * 当正在播放任意动画时，为了保证动画的连贯性，不进行tick中的同步，只在开始和结束时设置了同步，这样可以保证客户端tick不受服务端延迟影响
         *
         * 而不播放动画时，则要保证数据的同步
         */
        var sync = true

        // 当前动画计时器
        data.presentAnim?.let {
            if (data.targetAnim == null) {
                if (data.tick < data.maxTick) {
                    data.tick++
                    sync = false
                }
                else {
                    data.tick = 0
                    if (entity.animData.presentAnim?.name != entity.animData.defaultAnim?.name) stop() // 如果当前动画和要结束回到的默认动画一致就不暂停了
                }
            }
        }

        // 动画过渡计时器
        data.targetAnim?.let {
            if (data.transTick < data.maxTransTick) {
                data.transTick++
                sync = false
            }
            else {
                // 结束过渡后，位置刚好是目标动画的开始位置，直接设置动画时间等参数
                data.transTick = 0
                data.tick = 0
                data.presentAnim = data.targetAnim
                data.maxTick = ceil(data.lifeTime * 20).toInt() // 根据逻辑，targetAnim的设置必须依赖start和stop，而这两者都会输入lifetime，所以这里直接用来设置最大tick时间即可
                data.targetAnim = null
            }
        }

        // 初始化默认动画
        data.defaultAnim?.let {
            if (data.presentAnim == null && data.targetAnim == null) start(it)
        }

        if (sync) entity.syncAnimDataToClient()

    }

}