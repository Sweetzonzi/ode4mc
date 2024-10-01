package cn.solarmoon.spark_core.api.animation.sync

import cn.solarmoon.spark_core.api.animation.anim.IAnimatable
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.handling.IPayloadContext
import net.neoforged.neoforge.network.handling.IPayloadHandler

class AnimDataRefraction: IPayloadHandler<AnimNetData> {

    override fun handle(
        payload: AnimNetData,
        context: IPayloadContext
    ) {
        val level = context.player().level() as ClientLevel
        val entity = level.getEntity(payload.entityId)
        if (entity !is IAnimatable<*>) return
        val data = payload.animData
        entity.animData = data
    }

}