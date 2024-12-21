package cn.solarmoon.spirit_of_fight.feature.fight_skill.skill

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.anim.play.AnimModificationData
import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.animation.sync.SyncedAnimation
import cn.solarmoon.spark_core.api.entity.attack.getAttackedData
import cn.solarmoon.spark_core.api.entity.preinput.getPreInput
import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.FightSkillController
import cn.solarmoon.spirit_of_fight.feature.fight_skill.spirit.getFightSpirit
import cn.solarmoon.spirit_of_fight.feature.fight_skill.sync.ClientOperationPayload
import cn.solarmoon.spirit_of_fight.feature.fight_skill.sync.FightSpiritPayload
import cn.solarmoon.spirit_of_fight.feature.hit.HitType
import com.google.common.collect.HashBiMap
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

/**
 * @param attackChangeNode 变招节点，int为可以变到这一招的连招序列号，比如想在第0招可以变到第1招，那么输入1即可。double为变招到下一个连招动画的起始位置（比如0.0就是从当前连招动画位置过渡到下一个连招动画的开始，0.5则过渡到下一个连招动画的0.5s处）
 */
abstract class ComboAnimSkill(
    controller: FightSkillController,
    val animGroup: Map<Int, SyncedAnimation>,
    private val attackSwitchNode: Map<Int, Double>,
    val attackChangeNode: Map<Int, Double>,
    private val damageMultiplier: Map<Int, Float>,
    private val hitType: Map<Int, HitType>,
    private val hitStrength: Map<Int, Int>
): AttackAnimSkill(
    controller,
    buildSet { animGroup.values.forEach { add(it.anim.name) } }
) {

    companion object {
        @JvmStatic
        fun createComboConsumeAnims(prefix: String, maxCombo: Int): Map<Int, SyncedAnimation> = buildMap {
            for (i in 0 until maxCombo) {
                put(i, SyncedAnimation(MixedAnimation("$prefix:attack_$i", startTransSpeed = 6f)))
            }
        }
    }

    val animBiMap = HashBiMap.create(buildMap { animGroup.forEach { key, value -> put(key, value.anim.name) } })

    var index = 0

    abstract fun getMoveByIndex(index: Int, anim: MixedAnimation): Vec3?

    override fun getMove(anim: MixedAnimation): Vec3? {
        return getMoveByIndex(animBiMap.inverse()[anim.name]!!, anim)
    }

    override fun getHitType(anim: MixedAnimation): HitType {
        return hitType[animBiMap.inverse()[anim.name]]!!
    }

    override fun getHitStrength(anim: MixedAnimation): Int {
        return hitStrength[animBiMap.inverse()[anim.name]] ?: 0
    }

    fun start(changeTack: Boolean, sync: (SyncedAnimation) -> Unit = {}) {
        // 最后一段只在结束过渡时才能预输入，同时保证了index不会超过size值
        if (index <= animGroup.size - 1) entity.getPreInput().setInput("combo") {
            animGroup[index]?.let {
                it.consume(animatable, getAnimModifier(changeTack))
                sync.invoke(it)
            }
            index++
        }
    }

    fun getAnimModifier(changeTack: Boolean): AnimModificationData = AnimModificationData(
        getAttackAnimSpeed(baseAttackSpeed),
        if (changeTack) 2f else if (controller.isAttacking { !it.isCancelled } && !isPlaying()) 2f else -1f, // 特殊攻击到普通攻击的过渡时间上升
        if (changeTack) attackChangeNode[index]?.toFloat()?.times(20) ?: -1f else -1f
    )

    override fun whenInAnim(anim: MixedAnimation) {
        super.whenInAnim(anim)

        // 如果正在播放任何连击动画，按规定的结束点进行切换（预输入调用）
        val anim = anim.takeIf { !it.isCancelled } ?: return
        val id = animBiMap.inverse()[anim.name]!!
        val switch = attackSwitchNode[id] ?: anim.maxTick
        if (anim.isTickIn(switch, anim.maxTick)) {
            entity.getPreInput().executeIfPresent()
        }
    }

    override fun getAttackDamageMultiplier(anim: MixedAnimation): Float? {
        val index = animBiMap.inverse()[anim.name]!!
        return damageMultiplier[index]
    }

    override fun addFightSpiritWhenAttack(target: Entity) {
        getPlayingAnim()?.let { anim ->
            var mul = getAttackDamageMultiplier(anim) ?: 1f
            val fs = entity.getFightSpirit()
            if (target.getAttackedData() == null) mul /= 2 // 没有受击数据则意味着格挡成功，数据已被清除，此时增值除以2
            fs.addStage(mul)
        }
    }

}