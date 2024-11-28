package cn.solarmoon.spirit_of_fight.feature.fight_skill.controller

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.phys.obb.OrientedBoundingBox
import cn.solarmoon.spark_core.api.visual_effect.common.trail.Trail
import cn.solarmoon.spark_core.registry.client.SparkVisualEffectRenderers
import cn.solarmoon.spirit_of_fight.feature.fight_skill.skill.ComboAnimSkill
import cn.solarmoon.spirit_of_fight.feature.fight_skill.skill.ConcentrationAttackAnimSkill
import cn.solarmoon.spirit_of_fight.feature.fight_skill.skill.DodgeAnimSkill
import cn.solarmoon.spirit_of_fight.feature.fight_skill.skill.GuardAnimSkill
import cn.solarmoon.spirit_of_fight.feature.fight_skill.skill.JumpAttackAnimSkill
import cn.solarmoon.spirit_of_fight.feature.fight_skill.skill.SingleAttackAnimSkill
import cn.solarmoon.spirit_of_fight.feature.fight_skill.skill.SprintAttackAnimSkill
import cn.solarmoon.spirit_of_fight.feature.hit.HitType
import net.minecraft.core.Direction
import net.minecraft.tags.ItemTags
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f

class SwordFightSkillController(animatable: IEntityAnimatable<*>): FightSkillController(
    animatable,
    NAME,
    1.6f,
    Vector3f(0.5f, 0.5f, 1f)
) {

    companion object {
        @JvmStatic
        fun registerAnim() {}
        @JvmStatic
        val NAME = "sword"
        @JvmStatic
        val COMBO_ANIMS = ComboAnimSkill.createComboConsumeAnims(NAME, 3)
        @JvmStatic
        val GUARD_ANIMS = GuardAnimSkill.createGuardConsumeAnim(NAME)
        @JvmStatic
        val DODGE_ANIMS = DodgeAnimSkill.createDodgeConsumeAnims(NAME)
        @JvmStatic
        val JUMP_ATTACK_ANIM = SingleAttackAnimSkill.createSingleAttackConsumeAnim(NAME, "jump")
        @JvmStatic
        val SPRINTING_ATTACK_ANIM = SingleAttackAnimSkill.createSingleAttackConsumeAnim(NAME, "sprinting")
        @JvmStatic
        val SPECIAL_ATTACK_ANIM = SingleAttackAnimSkill.createSingleAttackConsumeAnim(NAME, "special")
    }

    override val isAvailable: Boolean
        get() {
            val entity = entity
            return entity is LivingEntity && entity.mainHandItem.`is`(ItemTags.SWORDS)
        }

    override val combo: ComboAnimSkill = object : ComboAnimSkill(
        this@SwordFightSkillController,
        COMBO_ANIMS,
        mapOf(0 to 0.5, 1 to 0.4),
        mapOf(1 to 0.15),
        mapOf(2 to 1.5f),
        mapOf(0 to HitType.LIGHT_CHOP, 1 to HitType.LIGHT_SWIPE, 2 to HitType.HEAVY_STAB)
    ) {
        override fun shouldBoxSummon(index: Int, anim: MixedAnimation): Boolean {
            val attackTimes = mapOf(
                0 to Pair(0.25, 0.5),
                1 to Pair(0.2, 0.4),
                2 to Pair(0.2, 0.5)
            )
            return attackTimes[index]?.let { (start, end) ->
                anim.isTickIn(start, end)
            } == true
        }

        override fun getMoveByIndex(index: Int, anim: MixedAnimation): Vec3? {
            val attackParameters = mutableMapOf<Int, MutableList<Pair<Pair<Double, Double>, Vec3?>>>()
            fun add(index: Int, timestamp: Pair<Double, Double>, move: Vec3?) = attackParameters.computeIfAbsent(index) { mutableListOf() }.add(Pair(timestamp, move))
            add(0, Pair(0.15, 0.3), getForwardMoveVector(1/8f))
            add(1, Pair(0.20, 0.3), getForwardMoveVector(1/10f))
            add(2, Pair(0.15, 0.3), getForwardMoveVector(1/6f))
            return attackParameters[index]?.firstOrNull { (range, _) ->
                anim.isTickIn(range.first, range.second)
            }?.second
        }
    }

    override val dodge: DodgeAnimSkill = DodgeAnimSkill(animatable, DODGE_ANIMS, 0.35)

    override val guard: GuardAnimSkill = GuardAnimSkill(this@SwordFightSkillController, GUARD_ANIMS, 150.0)

    override val jumpAttack = object : JumpAttackAnimSkill(this@SwordFightSkillController, JUMP_ATTACK_ANIM, 1.25f, 0.55, HitType.LIGHT_CHOP) {
        override fun getBox(anim: MixedAnimation): List<OrientedBoundingBox> {
            return if (anim.isTickIn(0.15, 0.45)) super.getBox(anim) else listOf()
        }

        override fun getMove(anim: MixedAnimation): Vec3? {
            return null
        }
    }

    override val sprintAttack = object : SprintAttackAnimSkill(this@SwordFightSkillController, SPRINTING_ATTACK_ANIM, 1.25f, 0.55, HitType.LIGHT_SWIPE) {
        override fun getBox(anim: MixedAnimation): List<OrientedBoundingBox> {
            return if (anim.isTickIn(0.25, 0.55)) super.getBox(anim) else listOf()
        }

        override fun getMove(anim: MixedAnimation): Vec3? {
            return if (anim.isTickIn(0.0, 0.25)) getForwardMoveVector(1/5f) else if (anim.isTickIn(0.25, 0.55)) getForwardMoveVector(1/2.5f) else null
        }
    }

    val specialAttack = object : ConcentrationAttackAnimSkill(this@SwordFightSkillController, SPECIAL_ATTACK_ANIM, 1.5f, 1.6, HitType.KNOCKDOWN_CHOP) {
        override fun getBox(anim: MixedAnimation): List<OrientedBoundingBox> {
            return if (anim.isTickIn(0.1, 0.5) || anim.isTickIn(0.95, 1.25)) super.getBox(anim) else listOf()
        }

        override fun onBoxSummon(box: OrientedBoundingBox, anim: MixedAnimation) {
            super.onBoxSummon(box, anim)
            if (entity.level().isClientSide) SparkVisualEffectRenderers.TRAIL.setAdd(getBoxId()) {
                Trail(getBoundBox(anim, it), Direction.Axis.Z).apply {
                    if (entity is LivingEntity) setTexture(entity.mainHandItem)
                }
            }
        }

        override fun onBoxNotPresent(anim: MixedAnimation?) {
            if (entity.level().isClientSide) SparkVisualEffectRenderers.TRAIL.clearAdd(getBoxId())
        }

        override fun getMove(anim: MixedAnimation): Vec3? {
            return null
        }

        override fun getAttackDamageMultiplier(anim: MixedAnimation): Float? {
            return if (anim.isTickIn(0.0, 0.5)) 1f else 2f
        }
    }

    override val specialAttackSkillGroup: MutableList<SingleAttackAnimSkill>
        get() = super.specialAttackSkillGroup.apply { add(specialAttack) }

}