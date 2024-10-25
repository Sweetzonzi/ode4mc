package cn.solarmoon.spark_core.api.util

import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.food.FoodProperties

object PlayerUtil {//

    /**
     * 快速让玩家吃下FoodProperties的所有效果<br></br>
     * 同时还有音效
     */
    @JvmStatic
    fun eat(player: Player, foodProperties: FoodProperties) {
        player.foodData.eat(foodProperties.nutrition, foodProperties.saturation)
        val effects = foodProperties.effects
        for (effectP in effects) {
            val random = player.random
            if (random.nextFloat() <= effectP.probability) {
                player.addEffect(effectP.effect())
            }
        }
        player.level().playSound(player, player.onPos.above(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1.0f, 1.0f)
    }

}