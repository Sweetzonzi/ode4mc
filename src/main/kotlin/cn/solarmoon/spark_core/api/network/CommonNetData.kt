package cn.solarmoon.spark_core.api.network

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.data.SerializeHelper
import net.minecraft.core.BlockPos
import net.minecraft.core.UUIDUtil
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler
import java.util.UUID


class CommonNetData(//
    val block: Block = Blocks.AIR,
    val pos: BlockPos = BlockPos(0, 0, 0),
    val vec3: Vec3 = Vec3.ZERO,
    val vec2: Vec2 = Vec2.ZERO,
    val itemStack: ItemStack = ItemStack.EMPTY,
    val fluidStack: FluidStack = FluidStack.EMPTY,
    val intValue: Int = 0,
    val floatValue: Float = 0f,
    val uuid: UUID = UUID.randomUUID(),
    val message: String = ""
): CustomPacketPayload {

    companion object {
        @JvmStatic
        val TYPE = CustomPacketPayload.Type<CommonNetData>(ResourceLocation.fromNamespaceAndPath(SparkCore.MOD_ID, "common_network_data"))

        @JvmStatic
        val STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, CommonNetData> {
            override fun decode(buffer: RegistryFriendlyByteBuf): CommonNetData {
                val block = SerializeHelper.BLOCK_STREAM_CODEC.decode(buffer)
                val pos = BlockPos.STREAM_CODEC.decode(buffer)
                val vec3 = SerializeHelper.VEC3_STREAM_CODEC.decode(buffer)
                val vec2 = SerializeHelper.VEC2_STREAM_CODEC.decode(buffer)
                val itemStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer)
                val fluidStack = FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer)
                val intValue = ByteBufCodecs.INT.decode(buffer)
                val floatValue = ByteBufCodecs.FLOAT.decode(buffer)
                val uuid = UUIDUtil.STREAM_CODEC.decode(buffer)
                val message = ByteBufCodecs.STRING_UTF8.decode(buffer)
                return CommonNetData(block, pos, vec3, vec2, itemStack, fluidStack, intValue, floatValue, uuid, message)
            }

            override fun encode(buffer: RegistryFriendlyByteBuf, value: CommonNetData) {
                SerializeHelper.BLOCK_STREAM_CODEC.encode(buffer, value.block)
                BlockPos.STREAM_CODEC.encode(buffer, value.pos)
                SerializeHelper.VEC3_STREAM_CODEC.encode(buffer, value.vec3)
                SerializeHelper.VEC2_STREAM_CODEC.encode(buffer, value.vec2)
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, value.itemStack)
                FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, value.fluidStack)
                ByteBufCodecs.INT.encode(buffer, value.intValue)
                ByteBufCodecs.FLOAT.encode(buffer, value.floatValue)
                UUIDUtil.STREAM_CODEC.encode(buffer, value.uuid)
                ByteBufCodecs.STRING_UTF8.encode(buffer, value.message)
            }
        }

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
