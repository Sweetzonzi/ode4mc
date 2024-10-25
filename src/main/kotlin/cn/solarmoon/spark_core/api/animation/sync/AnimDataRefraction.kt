package cn.solarmoon.spark_core.api.animation.sync

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.anim.play.AnimData
import net.minecraft.client.multiplayer.ClientLevel
import net.neoforged.neoforge.network.handling.IPayloadContext
import net.neoforged.neoforge.network.handling.IPayloadHandler
import kotlin.math.abs

class AnimDataRefraction: IPayloadHandler<AnimNetData> {

    override fun handle(
        payload: AnimNetData,
        context: IPayloadContext
    ) {
        val level = context.player().level() as ClientLevel
        val data = payload.animData
        val entity = level.getEntity(payload.id)
        if (entity !is IEntityAnimatable<*>) return
        entity.animData = data
    }

}