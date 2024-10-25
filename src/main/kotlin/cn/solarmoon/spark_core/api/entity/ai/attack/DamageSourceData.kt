package cn.solarmoon.spark_core.api.entity.ai.attack

import cn.solarmoon.spark_core.api.data.SerializeHelper
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.phys.Vec3
import java.util.Optional

data class DamageSourceData(
    val damageType: Holder<DamageType>,
    val directEntityId: Int?,
    val causingEntityId: Int?,
    val damageSourcePosition: Vec3?
) {

    companion object {
        @JvmStatic
        val CODEC: Codec<DamageSourceData> = RecordCodecBuilder.create {
            it.group(
                DamageType.CODEC.fieldOf("type").forGetter { it.damageType },
                Codec.INT.optionalFieldOf("direct_id").forGetter { Optional.ofNullable(it.directEntityId) },
                Codec.INT.optionalFieldOf("causing_id").forGetter { Optional.ofNullable(it.causingEntityId) },
                Vec3.CODEC.optionalFieldOf("position").forGetter { Optional.ofNullable(it.damageSourcePosition) }
            ).apply(it) { t, d, c, p ->
                DamageSourceData(t, d.orElse(null), c.orElse(null), p.orElse(null))
            }
        }

        @JvmStatic
        val STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, DamageSourceData> {
            override fun decode(buffer: RegistryFriendlyByteBuf): DamageSourceData {
                val type = DamageType.STREAM_CODEC.decode(buffer)
                val dId = buffer.readNullable(ByteBufCodecs.INT)
                val cId = buffer.readNullable(ByteBufCodecs.INT)
                var pos: Vec3? = null
                if (buffer.readBoolean()) {
                    pos = SerializeHelper.VEC3_STREAM_CODEC.decode(buffer)
                }
                return DamageSourceData(type, dId, cId, pos)
            }

            override fun encode(buffer: RegistryFriendlyByteBuf, value: DamageSourceData) {
                DamageType.STREAM_CODEC.encode(buffer, value.damageType)
                buffer.writeNullable(value.directEntityId, ByteBufCodecs.INT)
                buffer.writeNullable(value.causingEntityId, ByteBufCodecs.INT)
                buffer.writeBoolean(value.damageSourcePosition != null)
                if (value.damageSourcePosition != null) {
                    SerializeHelper.VEC3_STREAM_CODEC.encode(buffer, value.damageSourcePosition)
                }
            }
        }
    }

}
