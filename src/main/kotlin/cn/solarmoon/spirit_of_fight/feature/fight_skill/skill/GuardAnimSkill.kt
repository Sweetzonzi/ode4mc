package cn.solarmoon.spirit_of_fight.feature.fight_skill.skill

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.sync.SyncedAnimation
import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.animation.anim.template.EntityStateAnim
import cn.solarmoon.spark_core.api.entity.attack.clearAttackedData
import cn.solarmoon.spark_core.api.entity.attack.getAttackedData
import cn.solarmoon.spark_core.api.phys.obb.MountableOBB
import cn.solarmoon.spark_core.api.phys.obb.OrientedBoundingBox
import cn.solarmoon.spark_core.registry.client.SparkVisualEffectRenderers
import cn.solarmoon.spark_core.api.entity.skill.AnimSkill
import cn.solarmoon.spark_core.api.entity.preinput.getPreInput
import cn.solarmoon.spark_core.api.entity.skill.IBoxBoundToBoneAnimSkill
import cn.solarmoon.spark_core.api.entity.state.isInRangeFrontOf
import cn.solarmoon.spark_core.api.phys.obb.clearMountableOBB
import cn.solarmoon.spark_core.api.phys.obb.setMountableOBB
import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.FightSkillController
import cn.solarmoon.spirit_of_fight.feature.fight_skill.sync.MovePayload
import cn.solarmoon.spirit_of_fight.feature.hit.HitType
import cn.solarmoon.spirit_of_fight.feature.hit.getHitStrength
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d.div
import thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d.toVec3
import java.awt.Color

open class GuardAnimSkill(
    private val controller: FightSkillController,
    val animGroup: Map<AnimType, SyncedAnimation>,
    private val guardRange: Double,
): AnimSkill(
    controller.animatable,
    buildSet { animGroup.values.forEach { add(it.anim.name) } }
), IBoxBoundToBoneAnimSkill {

    override val boxSize: Vector3f = controller.commonBoxSize
    override val boxOffset: Vector3f = controller.commonBoxOffset
    override fun getBoundBoneName(anim: MixedAnimation): String = "rightItem"

    enum class AnimType { IDLE, HURT }

    companion object {
        @JvmStatic
        fun createGuardConsumeAnim(prefix: String): Map<AnimType, SyncedAnimation> = buildMap {
            AnimType.entries.forEach {
                val name = it.toString().lowercase()
                when(it) {
                    AnimType.IDLE -> put(it, SyncedAnimation(MixedAnimation("$prefix:guard_${name}", startTransSpeed = 2f)))
                    AnimType.HURT -> put(it, SyncedAnimation(MixedAnimation("$prefix:guard_${name}", startTransSpeed = 5f)))
                }
            }
        }
    }

    val idleAnim = animGroup[AnimType.IDLE]!!
    val hurtAnim = animGroup[AnimType.HURT]!!
    open val unblockableDamageTypes = mutableListOf(DamageTypes.EXPLOSION, DamageTypes.PLAYER_EXPLOSION)

    var isHoldingGuard = false

    val preInput get() = entity.getPreInput()

    fun isStanding(filter: (MixedAnimation) -> Boolean = {true}) = isPlaying { it.name == idleAnim.anim.name && filter.invoke(it) }

    fun isBacking(filter: (MixedAnimation) -> Boolean = {true}) = isPlaying { it.name == hurtAnim.anim.name && filter.invoke(it) }

    fun start(sync: (SyncedAnimation) -> Unit = {}) {
        preInput.setInput("guard") {
            isHoldingGuard = true
            idleAnim.consume(animatable)
            sync.invoke(idleAnim)
        }
    }

    fun stop(sync: (SyncedAnimation) -> Unit = {}) {
        if (preInput.id == "guard") preInput.clear()
        isHoldingGuard = false
        SyncedAnimation.STOP.consume(animatable)
        sync.invoke(SyncedAnimation.STOP)
    }

    override fun whenInAnim(anim: MixedAnimation) {
        super.whenInAnim(anim)

        // 击退动作的末尾续上站立动画
        if (anim.name == hurtAnim.anim.name) {
            if (anim.tick >= anim.maxTick && !isStanding()) {
                animatable.animController.stopAndAddAnimation(idleAnim.anim.apply { startTransSpeed = 4f })
            }
        }

        // 格挡行走时混合一个走路动画
        if (!anim.isCancelled && anim.name == idleAnim.anim.name) {
            val controller = animatable.animController
            fun tryPlay(state: EntityStateAnim): Boolean = state.tryPlay(animatable, 1, { it.name !in animBounds }, {
                it.speed = it.speed / 2
                it.boneBlacklist.addAll(listOf("rightItem", "rightArm", "leftItem", "leftArm"))
                it.startTransSpeed = 2f
            })
            if (!tryPlay(EntityStateAnim.WALK_BACK)) {
                if (!tryPlay(EntityStateAnim.WALK)) {
                    if (!EntityStateAnim.shouldPlayStateAnim(animatable) && (EntityStateAnim.WALK_BACK.isPlaying(animatable, 1) || EntityStateAnim.WALK.isPlaying(animatable, 1))) {
                        controller.stopAnimation(EntityStateAnim.WALK_BACK.animName, EntityStateAnim.WALK.animName)
                    }
                }
            }
        }
    }

    override fun getBox(anim: MixedAnimation): List<OrientedBoundingBox> {
        return if (!anim.isInTransition) {
            val box = getBoundBox(anim)
            entity.setMountableOBB(getBoxId(), MountableOBB(MountableOBB.Type.STRIKABLE_BONE, box))
            listOf(box)
        } else {
            entity.clearMountableOBB(getBoxId())
            listOf()
        }
    }

    override fun whenAttackedInAnim(damageSource: DamageSource, value: Float, anim: MixedAnimation): Boolean {
        val entity = entity
        if (entity.level().isClientSide) return true
        // 对于原版生物，只要在一个扇形范围内即可，对于lib的obb碰撞，则判断是否相交，同时如果受击数据不为空，那么以受击数据为准
        val attackedData = entity.getAttackedData()
        // 对于不可阻挡的伤害类型以及击打力度大于0的情况，不会被格挡成功
        if (unblockableDamageTypes.any { damageSource.typeHolder().`is`(it) } || (attackedData?.getHitStrength() ?: 0) > 0) {
            return true
        }
        // 如果受击数据里有guard，则免疫此次攻击
        val isBoxInteract = attackedData != null && attackedData.damageBone == getBoxId()
        // 如果受到box的攻击，位移以box中心为准，否则以直接攻击者的坐标位置为准
        val targetPos = attackedData?.damageBox?.center?.toVec3() ?: damageSource.sourcePosition ?: return true
        // 如果受到box的攻击，按防守盒是否被碰撞为准，否则以攻击者的坐标位置是否在指定扇形范围内为准
        val attackedCheck = if (attackedData != null) isBoxInteract else entity.isInRangeFrontOf(targetPos, guardRange)
        if (attackedCheck && isHoldingGuard) {
            SparkVisualEffectRenderers.OBB.syncBoxToClient(getBoxId(), Color.RED, null)
            if (getPlayingAnim{ !it.isCancelled }?.name == idleAnim.anim.name) {
                if (entity is LivingEntity) {
                    hurtAnim.consume(animatable)
                    hurtAnim.syncToClient(entity.id)
                    val v = Vec3(entity.x - targetPos.x, entity.y - targetPos.y, entity.z - targetPos.z).normalize().div(3.0)
                    MovePayload.moveEntityInClient(entity.id, v)
                }
            }
            entity.clearAttackedData()
            return false
        }
        return true
    }

}