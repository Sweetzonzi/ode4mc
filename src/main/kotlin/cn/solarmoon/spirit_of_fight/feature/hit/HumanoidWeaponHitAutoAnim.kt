package cn.solarmoon.spirit_of_fight.feature.hit

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.anim.auto_anim.HitAutoAnim
import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.animation.sync.SyncedAnimation
import cn.solarmoon.spark_core.api.entity.attack.clearAttackedData
import cn.solarmoon.spark_core.api.entity.attack.getAttackedData
import cn.solarmoon.spark_core.api.entity.state.getLateralSide
import cn.solarmoon.spark_core.api.entity.state.getSide
import cn.solarmoon.spark_core.api.phys.toVec3
import cn.solarmoon.spark_core.api.util.Side
import cn.solarmoon.spirit_of_fight.feature.hit.HitType.*
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent
import thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d.toVec3

class HumanoidWeaponHitAutoAnim(
    entity: Entity,
    animatable: IEntityAnimatable<*>
): HitAutoAnim(entity, animatable, PREFIX) {
    
    private var sourcePos: Vec3? = null

    override fun getAllAnimNames(): Set<String> {
        return ALL_ANIM_NAMES
    }

    override fun onActualHit(event: LivingDamageEvent.Post) {
        val level = entity.level()
        if (level.isClientSide) return

        sourcePos = event.source.sourcePosition

        if (getAnimSuffixName().isEmpty()) return
        val hitAnimName = getAnimName()

        if (!animatable.animData.animationSet.hasAnimation(hitAnimName)) return

        if (!isPlaying { it.isInTransition || it.name in ALL_KNOCKDOWN_ANIM_NAMES }) { // 只能在非过渡阶段以及非倒地动画才能切换受击动画
            val anim = ALL_SYNCED_ANIMATIONS[hitAnimName] ?: return
            anim.consume(animatable)
            anim.syncToClient(entity.id)
            entity.clearAttackedData()
            return
        }
    }

    override fun getAnimSuffixName(): String {
        entity.getAttackedData()?.let { data ->
            val sourcePos = sourcePos ?: return ""
            val hitType = data.getHitType() ?: return ""
            val side = entity.getLateralSide(data.damageBox.position.toVec3())
            val posSide = entity.getSide(sourcePos)
            val damagedBone = data.damageBone
            if (damagedBone != null) {
                val hitAnimName = getAnimName(hitType, damagedBone, side, posSide) ?: return ""
                return hitAnimName.substringAfter("/")
            }
        }
        return ""
    }

    companion object {
        @JvmStatic
        val PREFIX = "HumanWeaponHit"

        @JvmStatic
        fun getAnimName(hitType: HitType, boneName: String, hitSide: Side, posSide: Side): String? {
            val suffix = when (posSide) {
                Side.FRONT -> when (hitType) {
                    LIGHT_CHOP, HEAVY_CHOP, KNOCKDOWN_CHOP -> when (boneName) {
                        "head" -> "${hitType.getName()}:$boneName"
                        "waist", "leftArm", "rightArm" -> "${hitType.getName()}:body_${hitSide}"
                        else -> null
                    }
                    LIGHT_SWIPE, HEAVY_SWIPE, KNOCKDOWN_SWIPE -> when (boneName) {
                        "head" -> "${hitType.getName()}:${boneName}_$hitSide"
                        "waist", "leftArm", "rightArm" -> "${hitType.getName()}:body_$hitSide"
                        "leftLeg", "rightLeg" -> "${hitType.getName()}:leg_$hitSide"
                        else -> null
                    }
                    LIGHT_STAB, HEAVY_STAB, KNOCKDOWN_STAB -> when (boneName) {
                        "head" -> "${hitType.getName()}:${boneName}"
                        "waist", "leftArm", "rightArm" -> "${hitType.getName()}:body"
                        "leftLeg", "rightLeg" -> "${hitType.getName()}:leg"
                        else -> null
                    }
                    else -> null
                }
                else -> when (hitType) {
                    LIGHT_CHOP, LIGHT_SWIPE, LIGHT_STAB -> when (boneName) {
                        "head", "waist", "leftArm", "rightArm" -> "light_all:upperbody_$posSide"
                        "leftLeg", "rightLeg" -> "light_all:lowerbody_$posSide"
                        else -> null
                    }
                    HEAVY_CHOP, HEAVY_SWIPE, HEAVY_STAB -> when (boneName) {
                        "head", "waist", "leftArm", "rightArm" -> "heavy_all:upperbody_$posSide"
                        "leftLeg", "rightLeg" -> "heavy_all:lowerbody_$posSide"
                        else -> null
                    }
                    KNOCKDOWN_CHOP, KNOCKDOWN_SWIPE, KNOCKDOWN_STAB -> when (boneName) {
                        "head", "waist", "leftArm", "rightArm" -> "knockback_all:upperbody_$posSide"
                        "leftLeg", "rightLeg" -> "knockback_all:lowerbody_$posSide"
                        else -> null
                    }
                }
            }
            return suffix?.let { "$PREFIX/$it" }
        }
        
        @JvmStatic
        val ALL_ANIM_NAMES: Set<String> = buildSet {
            // 添加所有可能的 hitSide
            for (hitSide in listOf(Side.LEFT, Side.RIGHT)) {
                for (posSide in Side.entries) {
                    // 针对每种 HitType，获得所有可能的动画名称
                    for (hitType in HitType.entries) {
                        add(getAnimName(hitType, "head", hitSide, posSide) ?: "")
                        add(getAnimName(hitType, "waist", hitSide, posSide) ?: "")
                        add(getAnimName(hitType, "leftArm", hitSide, posSide) ?: "")
                        add(getAnimName(hitType, "rightArm", hitSide, posSide) ?: "")
                        add(getAnimName(hitType, "leftLeg", hitSide, posSide) ?: "")
                        add(getAnimName(hitType, "rightLeg", hitSide, posSide) ?: "")
                    }
                }
            }

            // 从结果中移除空的动画名称
            filterNot { it.isEmpty() }
        }

        @JvmStatic
        val ALL_SYNCED_ANIMATIONS = buildMap { ALL_ANIM_NAMES.forEach { put(it, SyncedAnimation(MixedAnimation(it, startTransSpeed = 6f))) } }

        @JvmStatic
        val ALL_KNOCKDOWN_ANIM_NAMES: Set<String> = buildSet {
            // 添加所有可能的 hitSide
            for (hitSide in listOf(Side.LEFT, Side.RIGHT)) {
                for (posSide in Side.entries) {
                    // 针对每种 HitType，获得所有可能的动画名称
                    for (hitType in (HitType.entries.filter { it.isKnockDown })) {
                        add(getAnimName(hitType, "head", hitSide, posSide) ?: "")
                        add(getAnimName(hitType, "waist", hitSide, posSide) ?: "")
                        add(getAnimName(hitType, "leftArm", hitSide, posSide) ?: "")
                        add(getAnimName(hitType, "rightArm", hitSide, posSide) ?: "")
                        add(getAnimName(hitType, "leftLeg", hitSide, posSide) ?: "")
                        add(getAnimName(hitType, "rightLeg", hitSide, posSide) ?: "")
                    }
                }
            }

            // 从结果中移除空的动画名称
            filterNot { it.isEmpty() }
        }

        @JvmStatic
        fun registerAnim() {}
    }

}