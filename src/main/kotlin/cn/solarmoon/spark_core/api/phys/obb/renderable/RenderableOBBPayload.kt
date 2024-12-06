package cn.solarmoon.spark_core.api.phys.obb.renderable

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.phys.obb.OrientedBoundingBox
import cn.solarmoon.spark_core.api.phys.thread.getPhysLevel
import cn.solarmoon.spark_core.registry.common.SparkVisualEffects
import kotlinx.coroutines.launch
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.network.handling.IPayloadContext
import java.awt.Color

data class RenderableOBBPayload(
    val id: String,
    val color: Int?,
    val box: OrientedBoundingBox?
): CustomPacketPayload {

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return TYPE
    }

    companion object {
        @JvmStatic
        fun handleInClient(payload: RenderableOBBPayload, context: IPayloadContext) {
            val debug = SparkVisualEffects.OBB.getRenderableBox(payload.id)
            payload.color?.let { debug.setColor(Color(it)) }
            payload.box?.let { debug.box = it }
        }

        @JvmStatic
        val TYPE = CustomPacketPayload.Type<RenderableOBBPayload>(ResourceLocation.fromNamespaceAndPath(SparkCore.MOD_ID, "box"))

        @JvmStatic
        val STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, RenderableOBBPayload> {
            override fun decode(buffer: RegistryFriendlyByteBuf): RenderableOBBPayload {
                val id = buffer.readUtf()
                val color = buffer.readNullable(ByteBufCodecs.INT)
                val box = buffer.readNullable(OrientedBoundingBox.Companion.STREAM_CODEC)
                return RenderableOBBPayload(id, color, box)
            }

            override fun encode(buffer: RegistryFriendlyByteBuf, value: RenderableOBBPayload) {
                buffer.writeUtf(value.id)
                buffer.writeNullable(value.color, ByteBufCodecs.INT)
                buffer.writeNullable(value.box, OrientedBoundingBox.Companion.STREAM_CODEC)
            }
        }
    }

}