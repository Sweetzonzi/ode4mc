package cn.solarmoon.spark_core.api.animation.sync

import cn.solarmoon.spark_core.api.animation.anim.AnimationSet
import cn.solarmoon.spark_core.api.animation.model.CommonModel
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.handling.IPayloadContext
import net.neoforged.neoforge.network.handling.IPayloadHandler

class WholeModelDataRefraction: IPayloadHandler<WholeModelData> {

    override fun handle(
        payload: WholeModelData,
        context: IPayloadContext
    ) {
        payload.models.forEach { id, model ->
            CommonModel.ORIGINS[id] = model
        }
        payload.animationSets.forEach { id, anim ->
            AnimationSet.ORIGINS[id] = anim
        }
    }

    @SubscribeEvent
    fun sendWholeModelWhenPlayerJoinWorld(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity
        val level = player.level()
        if (level is ServerLevel) {
            // 刚上线时同步基础的原始模型数据
            PacketDistributor.sendToPlayer(player as ServerPlayer, WholeModelData(CommonModel.ORIGINS, AnimationSet.ORIGINS))
        }
    }

}