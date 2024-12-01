package cn.solarmoon.spirit_of_fight.feature.fight_skill.skill

import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.entity.attack.getAttackedData
import cn.solarmoon.spark_core.api.entity.skill.AnimSkill
import cn.solarmoon.spark_core.api.entity.skill.IBoxBoundToBoneAnimSkill
import cn.solarmoon.spark_core.api.phys.obb.OrientedBoundingBox
import cn.solarmoon.spark_core.api.visual_effect.common.trail.Trail
import cn.solarmoon.spark_core.registry.common.SparkVisualEffects
import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.FightSkillController
import cn.solarmoon.spirit_of_fight.feature.hit.HitType
import cn.solarmoon.spirit_of_fight.feature.hit.setHitStrength
import cn.solarmoon.spirit_of_fight.feature.hit.setHitType
import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import org.joml.Vector3f
import java.awt.Color

/**
 * 拥有以下部分：
 *
 * - 攻击时对战意的修改
 * - 默认将碰撞箱绑定到右手骨骼上
 * - 重攻击的屏幕颤动
 * - 刀光
 * - 碰撞箱生成时自动攻击
 * - 自动根据生物触及距离拓展攻击碰撞箱
 * - 攻击力度（力度大于0时为不可格挡的攻击，然后数值越大将获得越大的屏幕震动）
 */
abstract class AttackAnimSkill(
    protected val controller: FightSkillController,
    animBounds: Set<String>,
): AnimSkill(
    controller.animatable,
    animBounds
), IBoxBoundToBoneAnimSkill {

    val baseAttackSpeed = controller.baseAttackSpeed
    override val boxSize: Vector3f = controller.commonBoxSize
    override val boxOffset: Vector3f = controller.commonBoxOffset

    override fun getBoundBoneName(anim: MixedAnimation): String = "rightItem"

    abstract fun getHitType(anim: MixedAnimation): HitType

    abstract fun getHitStrength(anim: MixedAnimation): Int

    override fun onBoxSummon(box: OrientedBoundingBox, anim: MixedAnimation) {
        super.onBoxSummon(box, anim)
        attack(box)
        if (entity.level().isClientSide) SparkVisualEffects.TRAIL.setAdd(getBoxId()) {
            val color = if (getHitStrength(anim) > 0) Color.RED else Color.WHITE
            Trail(getBoundBox(anim, it), Direction.Axis.Z, color).apply {
                if (entity is LivingEntity) getAttackItem(entity.weaponItem, anim)?.let { setTexture(it) }
            }
        }
    }

    override fun onFirstTargetAttacked(target: Entity) {
        super.onFirstTargetAttacked(target)
        animatable.animController.startFreezing(true)
        // 重攻击可使屏幕晃动
        getPlayingAnim()?.let {
            if (getHitType(it).isHeavy) SparkVisualEffects.CAMERA_SHAKE.shakeToClient(entity, 2, getHitStrength(it) + 0.5f)
        }
    }

    override fun onTargetAttacked(target: Entity) {
        super.onTargetAttacked(target)
        addFightSpiritWhenAttack(target)
        getPlayingAnim()?.let {
            target.getAttackedData()?.setHitType(getHitType(it))
            target.getAttackedData()?.setHitStrength(getHitStrength(it))
        }
    }

    abstract fun addFightSpiritWhenAttack(target: Entity)

}