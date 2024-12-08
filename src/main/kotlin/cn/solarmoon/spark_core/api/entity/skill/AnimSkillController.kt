package cn.solarmoon.spark_core.api.entity.skill

import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import kotlin.properties.Delegates

abstract class AnimSkillController {

    abstract val isAvailable: Boolean

    abstract val skillGroup: List<AnimSkill>

    private var isLoadedMoment by Delegates.observable(false) { _, old, new -> if (old != new && new == true) onLoadedMoment() }
    private var isDisabledMoment by Delegates.observable(false) { _, old, new -> if (old != new && new == true) onDisabledMoment() }

    val allSkillAnims: Set<String> get() {
        val list = mutableSetOf<String>()
        skillGroup.forEach { list.addAll(it.animBounds) }
        return list
    }

    open fun isAttacking(filter: (MixedAnimation) -> Boolean = {true}) = false

    fun isPlayingSkill(filter: (MixedAnimation) -> Boolean = {true}): Boolean {
        return skillGroup.any { it.isPlaying(filter) }
    }

    fun getPlayingSkillAnim(filter: (MixedAnimation) -> Boolean = {true}): MixedAnimation? {
        return skillGroup.map { it.getPlayingAnim(filter) }.firstOrNull { it != null }
    }

    inline fun <reified T: AnimSkill> getPlayingSkill(): T? {
        val skill = skillGroup.firstOrNull { it.getPlayingAnim() != null }
        return skill as? T
    }

    open fun tick() {
        if (isAvailable) {
            skillGroup.forEach { it.tick() }
        }
    }

    open fun physTick() {
        if (!isAvailable) {
            isLoadedMoment = false
            isDisabledMoment = true
            whenDisabled()
            return
        }

        isLoadedMoment = true
        isDisabledMoment = false

        skillGroup.forEach { it.physTick() }
    }

    /**
     * 当此技能控制器有效的一瞬间调用此方法
     */
    open fun onLoadedMoment() {}

    /**
     * 当此技能控制器失效的一瞬间调用此方法
     */
    open fun onDisabledMoment() {}

    /**
     * 当[isAvailable]为false时在tick中执行此方法
     */
    open fun whenDisabled() {}

}