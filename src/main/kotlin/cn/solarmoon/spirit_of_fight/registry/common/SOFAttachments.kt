package cn.solarmoon.spirit_of_fight.registry.common

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spirit_of_fight.feature.fight_skill.spirit.FightSpirit

object SOFAttachments {
    @JvmStatic
    fun register() {}

    @JvmStatic
    val FIGHT_SPIRIT = SparkCore.REGISTER.attachment<FightSpirit>()
        .id("fight_spirit")
        .defaultValue { FightSpirit() }
        .serializer { it.serialize(FightSpirit.CODEC) }
        .build()

}