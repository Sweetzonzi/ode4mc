package cn.solarmoon.spirit_of_fight.feature.hit

import cn.solarmoon.spark_core.entity.attack.AttackedData

fun AttackedData.getHitType() = extraData.getString("hitType").takeIf { it.isNotEmpty() }?.let { HitType.valueOf(it) }

fun AttackedData.setHitType(hitType: HitType) {
    extraData.putString("hitType", hitType.toString())
}

fun AttackedData.getHitStrength() = extraData.getInt("hitStrength")

fun AttackedData.setHitStrength(strength: Int) {
    extraData.putInt("hitStrength", strength)
}