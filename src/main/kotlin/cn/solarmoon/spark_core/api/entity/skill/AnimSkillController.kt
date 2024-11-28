package cn.solarmoon.spark_core.api.entity.skill

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import net.minecraft.world.item.ItemStack

abstract class AnimSkillController {

    abstract val isAvailable: Boolean

    abstract val skillGroup: List<AnimSkill>

    private var isLoadedMoment = false
    private var isDisabledMoment = false

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

    /**
     * 同[AnimSkill.getAttackDamageMultiplier]，只是整合到一起，会返回第一个符合条件的动画所返回的非null值
     */
    fun getAttackDamageMultiplier(filter: (MixedAnimation) -> Boolean = {true}): Float {
        for (skill in skillGroup) {
            val anim = skill.getPlayingAnim(filter) ?: continue
            val value = skill.getAttackDamageMultiplier(anim) ?: continue
            return value
        }
        return 1f
    }

    open fun tick() {
        if (!isAvailable) {
            isLoadedMoment = false
            if (!isDisabledMoment) {
                isDisabledMoment = true
                onDisabledMoment()
            }
            whenDisabled()
            return
        } else isDisabledMoment = false

        if (!isLoadedMoment && isAvailable) {
            isLoadedMoment = true
            onLoadedMoment()
        }

        skillGroup.forEach { it.tick() }
    }

    /**
     * 当此技能控制器有效的一瞬间调用此方法
     */
    open fun onLoadedMoment() {}

    /**
     * 当[isAvailable]为false时在tick中执行此方法
     */
    open fun whenDisabled() {}

    /**
     * 当此技能控制器失效的一瞬间调用此方法
     */
    open fun onDisabledMoment() {}

}