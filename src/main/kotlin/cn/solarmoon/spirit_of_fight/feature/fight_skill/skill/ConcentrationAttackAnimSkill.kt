package cn.solarmoon.spirit_of_fight.feature.fight_skill.skill

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.sync.SyncedAnimation
import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.FightSkillController
import cn.solarmoon.spirit_of_fight.feature.fight_skill.spirit.getFightSpirit
import cn.solarmoon.spirit_of_fight.feature.hit.HitType
import net.minecraft.world.entity.Entity
import org.joml.Vector3f

abstract class ConcentrationAttackAnimSkill(
    controller: FightSkillController,
    attackAnim: SyncedAnimation,
    damageMultiplier: Float,
    switchTime: Double,
    hitType: HitType
): SingleAttackAnimSkill(controller, attackAnim, damageMultiplier, switchTime, hitType) {

    override val isMetCondition: Boolean
        get() = entity.getFightSpirit().isFull

    override fun onStart() {
        entity.getFightSpirit().clear()
    }

    override fun addFightSpiritWhenAttack(target: Entity) {}

}