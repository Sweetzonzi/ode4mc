package cn.solarmoon.spirit_of_fight.feature.fight_skill.skill

import cn.solarmoon.spark_core.animation.IEntityAnimatable
import cn.solarmoon.spark_core.animation.anim.auto_anim.EntityStateAutoAnim
import cn.solarmoon.spark_core.animation.anim.auto_anim.getAutoAnim
import cn.solarmoon.spark_core.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.skill.Skill
import cn.solarmoon.spark_core.skill.SkillType
import cn.solarmoon.spirit_of_fight.fighter.getEntityPatch
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent

class ParryAnimSkill(
    animatable: IEntityAnimatable<*>,
    skillType: SkillType<IEntityAnimatable<*>, out Skill<IEntityAnimatable<*>>>,
    animName: String,
    guardRange: Double
): GuardAnimSkill(animatable, skillType, animName, guardRange) {

    override fun onActivate() {
        holder.animController.stopAndAddAnimation(MixedAnimation(animName, startTransSpeed = 6f))
    }

    override fun onUpdate() {
        holder.animData.playData.getMixedAnimation(animName)?.takeIf { !it.isCancelled }?.let {
            entity.getEntityPatch().weaponGuardBody?.enable()
        } ?: run {
            end()
        }
    }

    override fun onSuccessGuard(attackerPos: Vec3, event: LivingIncomingDamageEvent): Boolean {
        return true
    }

}