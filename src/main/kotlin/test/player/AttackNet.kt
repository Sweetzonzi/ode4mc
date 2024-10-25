package test.player

import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.animation.vanilla.PlayerAnimHelper
import cn.solarmoon.spark_core.api.network.CommonNetData
import cn.solarmoon.spark_core.api.network.ICommonNetHandler
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.neoforged.neoforge.network.handling.IPayloadContext
import test.EES
import kotlin.random.Random

class AttackNet: ICommonNetHandler {
    override fun handle(
        payload: CommonNetData,
        context: IPayloadContext
    ) {
        val player = context.player() as ServerPlayer
        val level = player.serverLevel()
        when (payload.message) {
            "attack" -> {
                val animatable = PlayerAnimHelper.getAnimatable(player)
                val animData = animatable.animData
                val animName = animData.animationSet.animations[Random.nextInt(animData.animationSet.animations.size)].name
                animatable.animController.stopAndAddAnimation(MixedAnimation(animData.modelPath, animName, 1, 0.3f))
                animatable.syncAnimDataToClient()
            }
        }
    }

    class Else: ICommonNetHandler {
        override fun handle(
            payload: CommonNetData,
            context: IPayloadContext
        ) {

        }
    }
}