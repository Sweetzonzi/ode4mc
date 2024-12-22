package cn.solarmoon.spirit_of_fight.feature.fight_skill.skill

import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.animation.sync.SyncedAnimation
import cn.solarmoon.spark_core.api.entity.attack.getAttackedData
import cn.solarmoon.spark_core.api.entity.skill.AnimSkill
import cn.solarmoon.spark_core.api.entity.state.canSee
import cn.solarmoon.spark_core.api.phys.thread.getPhysWorld
import cn.solarmoon.spark_core.api.phys.toDVector3
import cn.solarmoon.spark_core.api.phys.toVec3
import cn.solarmoon.spark_core.registry.common.SparkVisualEffects
import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.FightSkillController
import cn.solarmoon.spirit_of_fight.feature.hit.getHitStrength
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.phys.Vec3
import org.ode4j.ode.OdeHelper
import java.awt.Color

abstract class GuardAnimSkill(
    controller: FightSkillController,
    animGroup: Set<String>,
    val guardRange: Double,
): AnimSkill(controller.animatable, animGroup) {

    val guardBox = OdeHelper.createBox(entity.getPhysWorld().space, controller.commonBoxSize.toDVector3())
    open val unblockableDamageTypes = mutableListOf(DamageTypes.EXPLOSION, DamageTypes.PLAYER_EXPLOSION)

    init {
//        boundingBones.add(
//            //AnimatableBone(animatable, "rightItem", mutableListOf(guardBox)) { it.forEach { it.offsetPosition = controller.commonBoxOffset.toDVector3() } }.apply { body.data().name = "guard" }
//        )
    }

    abstract fun start(sync: (SyncedAnimation) -> Unit = {})

    open fun stop(sync: (SyncedAnimation) -> Unit = {}) {
        SyncedAnimation.STOP.consume(animatable)
        sync.invoke(SyncedAnimation.STOP)
    }

//    override fun getBox(anim: MixedAnimation): List<OrientedBoundingBox> {
//        return if (shouldSummonBox(anim)) {
//            val box = getBoxBoundToBone(anim)
//            entity.setMountableOBB(getBoxId(), MountableOBB(MountableOBB.Type.STRIKABLE_BONE, box))
//            listOf(box)
//        } else {
//            listOf()
//        }
//    }
//
//    override fun onBoxNotPresent(anim: MixedAnimation?) {
//        super.onBoxNotPresent(anim)
//        entity.clearMountableOBB(getBoxId())
//    }

    override fun whenAttackedInAnim(damageSource: DamageSource, value: Float, anim: MixedAnimation): Boolean {
        val entity = entity
        SparkVisualEffects.CAMERA_SHAKE.shakeToClient(entity, 2, 0.5f)
        // 对于原版生物，只要在一个扇形范围内即可，对于lib的obb碰撞，则判断是否相交，同时如果受击数据不为空，那么以受击数据为准
        val attackedData = entity.getAttackedData()
        // 对于不可阻挡的伤害类型以及击打力度大于0的情况，不会被格挡成功
        if (unblockableDamageTypes.any { damageSource.typeHolder().`is`(it) } || (attackedData?.getHitStrength() ?: 0) > 0) {
            return true
        }
        // 如果受击数据里有guard，则免疫此次攻击
        val isBoxInteract = attackedData != null && attackedData.damagedBody?.name == "guard"
        // 如果受到box的攻击，位移以box中心为准，否则以直接攻击者的坐标位置为准
        val targetPos = attackedData?.damageBox?.position?.toVec3() ?: damageSource.sourcePosition ?: return true
        // 如果受到box的攻击，按防守盒是否被碰撞为准，否则以攻击者的坐标位置是否在指定扇形范围内为准
        val attackedCheck = if (attackedData != null) isBoxInteract else entity.canSee(targetPos, guardRange)
        if (attackedCheck) {
            SparkVisualEffects.OBB.syncBoxToClient("guard", Color.RED, null)
            return onSuccessGuard(targetPos, damageSource, value, anim)
        }

        return true
    }

    /**
     * @param attackerPos 如果攻击为碰撞箱触发，则为碰撞箱中心点，否则为伤害源坐标
     */
    abstract fun onSuccessGuard(attackerPos: Vec3, damageSource: DamageSource, value: Float, anim: MixedAnimation): Boolean

}