package cn.solarmoon.spirit_of_fight.fighter

import cn.solarmoon.spirit_of_fight.fighter.player.IPlayerPatchHolder
import cn.solarmoon.spirit_of_fight.fighter.player.PlayerPatch
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player

object FighterHelper {
}

@Suppress("unchecked_cast")
fun Player.getPatch() = (this as IPlayerPatchHolder).getPatch()

@Suppress("unchecked_cast")
fun Entity.getEntityPatch() = (this as IEntityPatchHolder).getEntityPatch()