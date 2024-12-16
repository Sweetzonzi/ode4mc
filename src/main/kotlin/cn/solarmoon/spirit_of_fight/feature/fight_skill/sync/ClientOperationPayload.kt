package cn.solarmoon.spirit_of_fight.feature.fight_skill.sync

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.data.SerializeHelper
import cn.solarmoon.spark_core.api.entity.preinput.getPreInput
import cn.solarmoon.spark_core.api.util.MoveDirection
import cn.solarmoon.spirit_of_fight.feature.fight_skill.IFightSkillHolder
import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.CommonFightSkillController
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.handling.IPayloadContext

data class ClientOperationPayload(
    val operation: String,
    val moveVector: Vec3,
    val id: Int
): CustomPacketPayload {

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return TYPE
    }

    companion object {
        @JvmStatic
        fun handleInServer(payload: ClientOperationPayload, context: IPayloadContext) {
            val player = context.player() as ServerPlayer
            if (player !is IFightSkillHolder) return
            val skill = player.skillController ?: return
            when(payload.operation) {
                "combo" -> {
                    skill.combo.start(false) {
                        it.syncToClientExceptPresentPlayer(player, skill.combo.getAnimModifier(false))
                    }
                }
                "combo_switch" -> {
                    skill.combo.start(true) {
                        it.syncToClientExceptPresentPlayer(player, skill.combo.getAnimModifier(true))
                    }
                    player.getPreInput().executeIfPresent("combo")
                }
                "dodge" -> {
                    skill.dodge.start(MoveDirection.getById(payload.id), payload.moveVector) {
                        it.syncToClientExceptPresentPlayer(player)
                    }
                }
                "guard" -> {
                    skill.guard.start {
                        it.syncToClientExceptPresentPlayer(player)
                    }
                }
                "guard_stop" -> {
                    skill.guard.stop {
                        it.syncToClientExceptPresentPlayer(player)
                    }
                }
                "guard_clear" -> {
                    (player as Entity).getPreInput().clear()
                }
                "parry" -> {
                    if (skill is CommonFightSkillController) skill.parry.start {
                        it.syncToClientExceptPresentPlayer(player)
                    }
                }
                else -> {
                    val operation = payload.operation.toIntOrNull() ?: return
                    skill.specialAttackSkillGroup.getOrNull(operation)?.let { attack ->
                        attack.start { it.syncToClientExceptPresentPlayer(player, attack.getAnimModifier()) }
                    }
                }
            }
        }

        @JvmStatic
        fun sendOperationToServer(operation: String, v: Vec3? = null, d: Int? = null) {
            PacketDistributor.sendToServer(ClientOperationPayload(operation, v ?: Vec3.ZERO, d ?: 0))
        }

        @JvmStatic
        val TYPE = CustomPacketPayload.Type<ClientOperationPayload>(ResourceLocation.fromNamespaceAndPath(SparkCore.MOD_ID, "client_operation"))

        @JvmStatic
        val STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, ClientOperationPayload> {
            override fun decode(buffer: RegistryFriendlyByteBuf): ClientOperationPayload {
                val operation = buffer.readUtf()
                val moveVector = SerializeHelper.VEC3_STREAM_CODEC.decode(buffer)
                val moveDirection = buffer.readInt()
                return ClientOperationPayload(operation, moveVector, moveDirection)
            }

            override fun encode(buffer: RegistryFriendlyByteBuf, value: ClientOperationPayload) {
                buffer.writeUtf(value.operation)
                SerializeHelper.VEC3_STREAM_CODEC.encode(buffer, value.moveVector)
                buffer.writeInt(value.id)
            }
        }
    }

}