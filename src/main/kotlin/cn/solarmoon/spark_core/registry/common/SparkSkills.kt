package cn.solarmoon.spark_core.registry.common

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.entity.skill.test.SimpleAttackAnimSkill
import org.joml.Vector3d

object SparkSkills {

    @JvmStatic
    fun register() {}

    @JvmStatic
    val PLAYER_SWORD_COMBO_0 = SparkCore.REGISTER.skill<SimpleAttackAnimSkill>()
        .id("player_sword_combo_0")
        .bound { SimpleAttackAnimSkill("sword:attack_0", Vector3d(0.5, 0.5, 1.0), Vector3d(0.0, 0.0, -0.5)) { _, anim -> anim.isTickIn(0.05, 0.25) } }
        .build()

    @JvmStatic
    val PLAYER_SWORD_COMBO_1 = SparkCore.REGISTER.skill<SimpleAttackAnimSkill>()
        .id("player_sword_combo_1")
        .bound { SimpleAttackAnimSkill("sword:attack_1", Vector3d(0.5, 0.5, 1.0), Vector3d(0.0, 0.0, -0.5)) { _, anim -> anim.isTickIn(0.05, 0.25) } }
        .build()

    @JvmStatic
    val PLAYER_SWORD_COMBO_2 = SparkCore.REGISTER.skill<SimpleAttackAnimSkill>()
        .id("player_sword_combo_2")
        .bound { SimpleAttackAnimSkill("sword:attack_2", Vector3d(0.5, 0.5, 1.0), Vector3d(0.0, 0.0, -0.5)) { _, anim -> anim.isTickIn(0.05, 0.25) } }
        .build()

}