package cn.solarmoon.spirit_of_fight.feature.hit

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.sync.SyncedAnimation
import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.util.Side
import com.mojang.serialization.Codec

/**
 * 受到的攻击类型
 */
enum class HitType {
    LIGHT_CHOP, HEAVY_CHOP, KNOCKDOWN_CHOP,
    LIGHT_SWIPE, HEAVY_SWIPE, KNOCKDOWN_SWIPE,
    LIGHT_STAB, HEAVY_STAB, KNOCKDOWN_STAB;

    fun getHitAnimName(boneName: String, hitSide: Side, posSide: Side): String? {
        return when (posSide) {
            Side.FRONT -> when (this) {
                LIGHT_CHOP, HEAVY_CHOP, KNOCKDOWN_CHOP -> when (boneName) {
                    "head" -> "${lowercase()}:$boneName"
                    "waist", "leftArm", "rightArm" -> "${lowercase()}:body_${hitSide}"
                    else -> null
                }
                LIGHT_SWIPE, HEAVY_SWIPE, KNOCKDOWN_SWIPE -> when (boneName) {
                    "head" -> "${lowercase()}:${boneName}_$hitSide"
                    "waist", "leftArm", "rightArm" -> "${lowercase()}:body_$hitSide"
                    "leftLeg", "rightLeg" -> "${lowercase()}:leg_$hitSide"
                    else -> null
                }
                LIGHT_STAB, HEAVY_STAB, KNOCKDOWN_STAB -> when (boneName) {
                    "head" -> "${lowercase()}:${boneName}"
                    "waist", "leftArm", "rightArm" -> "${lowercase()}:body"
                    "leftLeg", "rightLeg" -> "${lowercase()}:leg"
                    else -> null
                }
                else -> null
            }
            else -> when (this) {
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
    }

    val isKnockDown get() = this in listOf(KNOCKDOWN_STAB, KNOCKDOWN_CHOP, KNOCKDOWN_SWIPE)

    val isHeavy get() = this in listOf(HEAVY_CHOP, HEAVY_SWIPE, HEAVY_STAB)

    private fun lowercase() = toString().lowercase()

    companion object {
        @JvmStatic
        fun isPlayingHitAnim(animatable: IEntityAnimatable<*>, filter: (MixedAnimation) -> Boolean = { true }): Boolean {
            return ALL_HIT_ANIMATIONS.any {
                animatable.animController.isPlaying(it, filter)
            }
        }

        @JvmStatic
        fun isPlayingKnockDownAnim(animatable: IEntityAnimatable<*>, filter: (MixedAnimation) -> Boolean = { true }): Boolean {
            return ALL_KNOCKDOWN_ANIMATIONS.any {
                animatable.animController.isPlaying(it, filter)
            }
        }

        @JvmStatic
        val ALL_KNOCKDOWN_ANIMATIONS: Set<String> by lazy {
            val allHitAnimations = mutableSetOf<String>()

            // 添加所有可能的 hitSide
            for (hitSide in listOf(Side.LEFT, Side.RIGHT)) {
                for (posSide in Side.entries) {
                    // 针对每种 HitType，获得所有可能的动画名称
                    for (hitType in (HitType.entries.filter { it.isKnockDown })) {
                        allHitAnimations.add(hitType.getHitAnimName("head", hitSide, posSide) ?: "")
                        allHitAnimations.add(hitType.getHitAnimName("waist", hitSide, posSide) ?: "")
                        allHitAnimations.add(hitType.getHitAnimName("leftArm", hitSide, posSide) ?: "")
                        allHitAnimations.add(hitType.getHitAnimName("rightArm", hitSide, posSide) ?: "")
                        allHitAnimations.add(hitType.getHitAnimName("leftLeg", hitSide, posSide) ?: "")
                        allHitAnimations.add(hitType.getHitAnimName("rightLeg", hitSide, posSide) ?: "")
                    }
                }
            }

            // 从结果中移除空的动画名称
            allHitAnimations.filterNot { it.isEmpty() }.toSet()
        }

        @JvmStatic
        val ALL_HIT_ANIMATIONS: Set<String> by lazy {
            val allHitAnimations = mutableSetOf<String>()

            // 添加所有可能的 hitSide
            for (hitSide in listOf(Side.LEFT, Side.RIGHT)) {
                for (posSide in Side.entries) {
                    // 针对每种 HitType，获得所有可能的动画名称
                    for (hitType in HitType.entries) {
                        allHitAnimations.add(hitType.getHitAnimName("head", hitSide, posSide) ?: "")
                        allHitAnimations.add(hitType.getHitAnimName("waist", hitSide, posSide) ?: "")
                        allHitAnimations.add(hitType.getHitAnimName("leftArm", hitSide, posSide) ?: "")
                        allHitAnimations.add(hitType.getHitAnimName("rightArm", hitSide, posSide) ?: "")
                        allHitAnimations.add(hitType.getHitAnimName("leftLeg", hitSide, posSide) ?: "")
                        allHitAnimations.add(hitType.getHitAnimName("rightLeg", hitSide, posSide) ?: "")
                    }
                }
            }

            // 从结果中移除空的动画名称
            allHitAnimations.filterNot { it.isEmpty() }.toSet()
        }

        @JvmStatic
        val ALL_HIT_CONSUME_ANIMATIONS = buildMap { ALL_HIT_ANIMATIONS.forEach { put(it, SyncedAnimation(MixedAnimation(it, startTransSpeed = 5f))) } }

        @JvmStatic
        val CODEC: Codec<HitType> = Codec.STRING.xmap(
            { HitType.valueOf(it) }, { it.toString() }
        )

        @JvmStatic
        fun registerAnim() {}
    }

}