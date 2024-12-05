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

    val isKnockDown get() = this in listOf(KNOCKDOWN_STAB, KNOCKDOWN_CHOP, KNOCKDOWN_SWIPE)

    val isHeavy get() = this in listOf(HEAVY_CHOP, HEAVY_SWIPE, HEAVY_STAB)

    fun getName() = toString().lowercase()

}