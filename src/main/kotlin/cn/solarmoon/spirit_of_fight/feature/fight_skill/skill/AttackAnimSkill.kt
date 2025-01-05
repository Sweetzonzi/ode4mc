package cn.solarmoon.spirit_of_fight.feature.fight_skill.skill

import cn.solarmoon.spark_core.animation.IEntityAnimatable
import cn.solarmoon.spark_core.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.entity.preinput.getPreInput
import cn.solarmoon.spark_core.entity.state.getAttackAnimSpeed
import cn.solarmoon.spark_core.skill.BaseSkill
import cn.solarmoon.spark_core.skill.Skill
import cn.solarmoon.spark_core.skill.SkillType
import cn.solarmoon.spirit_of_fight.fighter.getEntityPatch
import net.minecraft.world.phys.Vec3
import org.ode4j.ode.DContactBuffer
import org.ode4j.ode.DGeom
import kotlin.let

open class AttackAnimSkill(
    animatable: IEntityAnimatable<*>,
    skillType: SkillType<IEntityAnimatable<*>, out Skill<IEntityAnimatable<*>>>,
    val animName: String,
    val baseAttackSpeed: Double,
    val switchNode: Double?,
    private val enableAttack: (IEntityAnimatable<*>, MixedAnimation) -> Boolean,
    private val enableMove: ((IEntityAnimatable<*>, MixedAnimation) -> Vec3?)
): BaseSkill<IEntityAnimatable<*>>(animatable, skillType) {

    val entity = animatable.animatable

    override fun onActivate() {
        val anim = MixedAnimation(animName, startTransSpeed = 6f, speed = entity.getAttackAnimSpeed(baseAttackSpeed.toFloat())).apply { shouldTurnBody = true }
        holder.animController.stopAndAddAnimation(anim)
    }

    override fun onUpdate() {
        val aBody = entity.getEntityPatch().weaponAttackBody ?: return
        holder.animData.playData.getMixedAnimation(animName)?.let {
            if (enableAttack.invoke(holder, it)) {
                aBody.enable()
            } else {
                aBody.disable()
            }

            switchNode?.let { time -> if (it.isTickIn(time, Double.MAX_VALUE)) entity.getPreInput().executeIfPresent() }

            enableMove.invoke(holder, it)?.let { entity.deltaMovement = it }

            if (it.isCancelled) end()
        } ?: run {
            end()
        }
    }

    override fun onEnd() {
        val aBody = entity.getEntityPatch().weaponAttackBody ?: return
        aBody.disable()
    }

    /**
     * 当击打到目标时调用（伤害触发前）
     */
    open fun whenAttacked(o1: DGeom, o2: DGeom, buffer: DContactBuffer) {
        holder.animController.startFreezing(false)
    }

}