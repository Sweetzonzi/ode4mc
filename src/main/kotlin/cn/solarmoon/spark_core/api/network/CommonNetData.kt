package cn.solarmoon.spark_core.api.network

import cn.solarmoon.spark_core.SparkCore
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler


class CommonNetData(
    val pos: BlockPos = BlockPos(0, 0, 0),
    val fluidStack: FluidStack = FluidStack.EMPTY,
    val message: String = ""
): CustomPacketPayload {

    companion object {
        @JvmStatic
        val TYPE = CustomPacketPayload.Type<CommonNetData>(ResourceLocation.fromNamespaceAndPath(SparkCore.MOD_ID, "common_network_data"))
        @JvmStatic
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, CommonNetData> = StreamCodec.composite(
            BlockPos.STREAM_CODEC, CommonNetData::pos,
            FluidStack.OPTIONAL_STREAM_CODEC, CommonNetData::fluidStack,
            ByteBufCodecs.STRING_UTF8, CommonNetData::message,
            ::CommonNetData
        )
        @JvmStatic
        fun register(event: RegisterPayloadHandlersEvent) {
            CommonNetRegister.HANDLERS.forEachIndexed { index, handlerPair ->
                val registrar = event.registrar((1.0 + index / 10.0).toString())
                registrar.playBidirectional(
                    TYPE,
                    STREAM_CODEC,
                    DirectionalPayloadHandler(handlerPair.first::handle, handlerPair.second::handle)
                )
            }
        }
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return TYPE
    }

}
