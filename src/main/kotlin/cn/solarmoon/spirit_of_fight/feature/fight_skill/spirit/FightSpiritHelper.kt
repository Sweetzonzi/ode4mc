package cn.solarmoon.spirit_of_fight.feature.fight_skill.spirit

import cn.solarmoon.spirit_of_fight.registry.common.SOFAttachments
import net.minecraft.world.entity.Entity

fun Entity.getFightSpirit() = getData(SOFAttachments.FIGHT_SPIRIT)