package cn.solarmoon.spirit_of_fight.feature.fight_skill.skill

import cn.solarmoon.spark_core.api.animation.anim.play.AnimModificationData
import cn.solarmoon.spark_core.api.animation.sync.SyncedAnimation
import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.entity.attack.getAttackedData
import cn.solarmoon.spark_core.api.entity.preinput.getPreInput
import cn.solarmoon.spark_core.api.entity.skill.IBoxBoundToBoneAnimSkill
import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.FightSkillController
import cn.solarmoon.spirit_of_fight.feature.fight_skill.spirit.getFightSpirit
import cn.solarmoon.spirit_of_fight.feature.fight_skill.sync.FightSpiritPayload
import cn.solarmoon.spirit_of_fight.feature.hit.HitType
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3

/**
 * @param damageMultiplier 除了应用在[getAttackDamageMultiplier]以外，还决定了可获得战意的倍率
 */
abstract class SingleAttackAnimSkill(
    controller: FightSkillController,
    val attackAnim: SyncedAnimation,
    private val damageMultiplier: Float,
    private val switchTime: Double,
    private val hitType: HitType,
    private val hitStrength: Int
): AttackAnimSkill(
    controller,
    setOf(attackAnim.anim.name)
), IBoxBoundToBoneAnimSkill {

    companion object {
        @JvmStatic
        fun createSingleAttackConsumeAnim(prefix: String, attackName: String): SyncedAnimation = SyncedAnimation(MixedAnimation("$prefix:attack_$attackName", startTransSpeed = 5f))
    }

    abstract val isMetCondition: Boolean

    val preInput get() = entity.getPreInput()

    fun start(sync: (SyncedAnimation) -> Unit = {}) {
        preInput.setInput(attackAnim.anim.name) {
            attackAnim.consume(animatable, getAnimModifier())
            sync.invoke(attackAnim)
            onStart()
        }
    }

    /**
     * 当开始播放动画的一瞬间调用
     */
    open fun onStart() {}

    open fun getAnimModifier() = AnimModificationData(getAttackAnimSpeed(baseAttackSpeed))

    abstract override fun getMove(anim: MixedAnimation): Vec3?

    override fun getHitType(anim: MixedAnimation): HitType {
        return hitType
    }

    override fun getHitStrength(anim: MixedAnimation): Int {
        return hitStrength
    }

    override fun getAttackDamageMultiplier(anim: MixedAnimation): Float? {
        return damageMultiplier
    }

    override fun getAttackItem(originWeapon: ItemStack?, anim: MixedAnimation): ItemStack? {
        return super.getAttackItem(originWeapon, anim)
    }

    override fun whenInAnim(anim: MixedAnimation) {
        super.whenInAnim(anim)

        if (preInput.hasInput() && !HitType.isPlayingHitAnim(animatable) { !it.isCancelled } ) {
            if (anim.isTickIn(switchTime, anim.maxTick)) {
                preInput.invokeInput()
            }
        }
    }

    override fun addFightSpiritWhenAttack(target: Entity) {
        val fs = entity.getFightSpirit()
        var mul = damageMultiplier
        if (target.getAttackedData() == null) mul /= 2 // 没有受击数据则意味着格挡成功，数据已被清除，此时增值除以2
        fs.addStage(mul)
        fs.syncToClient(entity.id, FightSpiritPayload.Type.ADD)
    }

}