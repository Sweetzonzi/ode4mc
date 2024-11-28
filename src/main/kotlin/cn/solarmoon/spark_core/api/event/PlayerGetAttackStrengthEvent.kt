package cn.solarmoon.spark_core.api.event

import net.minecraft.world.entity.player.Player
import net.neoforged.neoforge.event.entity.player.PlayerEvent

/**
 * 注入在[Player.getAttackStrengthScale]
 */
class PlayerGetAttackStrengthEvent(player: Player, val adjustTicks: Float, var attackStrengthScale: Float): PlayerEvent(player) {



}