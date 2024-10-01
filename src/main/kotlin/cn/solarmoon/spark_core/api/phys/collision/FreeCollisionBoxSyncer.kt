package cn.solarmoon.spark_core.api.phys.collision

import net.neoforged.neoforge.network.handling.IPayloadContext
import net.neoforged.neoforge.network.handling.IPayloadHandler
import java.awt.Color

object FreeCollisionBoxSyncer {

    class Client: IPayloadHandler<FreeCollisionBoxData> {
        override fun handle(
            payload: FreeCollisionBoxData,
            context: IPayloadContext
        ) {
            FreeCollisionBoxRenderManager(payload.id, payload.box, payload.lifetime, Color(payload.color)).start()
        }
    }

    class Server: IPayloadHandler<FreeCollisionBoxData> {
        override fun handle(
            payload: FreeCollisionBoxData,
            context: IPayloadContext
        ) {

        }
    }

}