package cn.solarmoon.spirit_of_fight.feature.fight_skill.skill

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.entity.attack.getAttackedData
import cn.solarmoon.spark_core.api.entity.skill.AnimSkill
import cn.solarmoon.spark_core.api.phys.AnimatableBone
import cn.solarmoon.spark_core.api.phys.IBoundingBone
import cn.solarmoon.spark_core.api.phys.data
import cn.solarmoon.spark_core.api.phys.livingCommonAttack
import cn.solarmoon.spark_core.api.phys.thread.getPhysWorld
import cn.solarmoon.spark_core.api.visual_effect.common.trail.Trail
import cn.solarmoon.spark_core.registry.common.SparkVisualEffects
import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.FightSkillController
import cn.solarmoon.spirit_of_fight.feature.hit.HitType
import cn.solarmoon.spirit_of_fight.feature.hit.setHitStrength
import cn.solarmoon.spirit_of_fight.feature.hit.setHitType
import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import org.ode4j.math.DVector3
import org.ode4j.ode.DGeom
import org.ode4j.ode.OdeHelper
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
 * - 强制位移（在控制器中按住s会阻止水平位移）
 */
abstract class AttackAnimSkill(
    val controller: FightSkillController,
    animBounds: Set<String>,
): AnimSkill(controller.animatable, animBounds) {

    val baseAttackSpeed = controller.baseAttackSpeed

    init {
        boundingBones.add(AnimatableBone(animatable, "rightItem", mutableListOf(createAttackBox(DVector3(0.5, 0.5, 3.0)))))
    }

    abstract fun getHitType(anim: MixedAnimation): HitType

    abstract fun getHitStrength(anim: MixedAnimation): Int

    override fun onGeomEnabled(bone: IBoundingBone) {
        super.onGeomEnabled(bone)
//        attack(box)
//        if (entity.level().isClientSide) SparkVisualEffects.TRAIL.setAdd(getBoxId()) {
//            val color = if (getHitStrength(anim) > 0) Color.RED else Color.WHITE
//            Trail(getBoxBoundToBone(anim, it), Direction.Axis.Z, color).apply {
//                if (entity is LivingEntity) getAttackItem(entity.weaponItem, anim)?.let { setTexture(it) }
//            }
//        }
    }

    override fun onGeomDisabled(bone: IBoundingBone) {
        super.onGeomDisabled(bone)
        bone.boundingGeoms.forEach { it.data().attackedEntities.clear() }
    }

    open fun onFirstTargetAttacked(target: Entity) {
        animatable.animController.startFreezing(true)
        // 重攻击可使屏幕晃动
        getPlayingAnim()?.let {
            if (getHitType(it).isHeavy) SparkVisualEffects.CAMERA_SHAKE.shakeToClient(entity, 2, getHitStrength(it) + 0.5f)
        }
    }

    open fun onTargetAttacked(target: Entity) {
        addFightSpiritWhenAttack(target)
        getPlayingAnim()?.let {
            target.getAttackedData()?.setHitType(getHitType(it))
            target.getAttackedData()?.setHitStrength(getHitStrength(it))
        }
    }

    fun createAttackBox(size: DVector3): DGeom = OdeHelper.createBox(entity.getPhysWorld().space, size).apply {
        data().onCollide { o2, buffer ->
            livingCommonAttack(o2, true)?.let {
                if (data().attackedEntities.size == 1) onFirstTargetAttacked(it)
                onTargetAttacked(it)
            }
            data().passFromCollision = true
        }
    }

    abstract fun addFightSpiritWhenAttack(target: Entity)

}