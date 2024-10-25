package cn.solarmoon.spark_core.api.entity.ai.attack

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.data.SerializeHelper
import cn.solarmoon.spark_core.api.phys.collision.FreeCollisionBox
import cn.solarmoon.spark_core.api.phys.collision.toOBB
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import java.util.Optional

/**
 * 受击信息
 * @param damageSourceData 包含了[DamageSource.directEntity]直接伤害源（如射出去的箭），和[DamageSource.causingEntity]造成伤害源（如射箭的玩家），以及伤害源位置信息
 * @param damageBox 触发该次攻击的box，可以通过box大小位置等信息实现想要的效果
 * @param damageBone 该次攻击的box所击中的骨骼
 */
data class AttackedData(
    val damageValue: Float,
    val damageSourceData: DamageSourceData,
    val damageBox: FreeCollisionBox,
    val damageBone: String?
) {
    constructor(damageValue: Float, damageSource: DamageSource, damageBox: FreeCollisionBox, damageBone: String?): this(
        damageValue,
        DamageSourceData(damageSource.typeHolder(), damageSource.directEntity?.id, damageSource.entity?.id, damageSource.sourcePosition),
        damageBox,
        damageBone
    )

    /**
     * 当将此值设为true时，将在生物的下一个tick删除该数据，以免多次调用该数据后对应的方法
     */
    var cancelled = false

    fun getDamageSource(level: Level): DamageSource {
        val dE = if (damageSourceData.directEntityId == null) null else level.getEntity(damageSourceData.directEntityId)
        val cE = if (damageSourceData.causingEntityId == null) null else level.getEntity(damageSourceData.causingEntityId)
        return DamageSource(damageSourceData.damageType, dE, cE, damageSourceData.damageSourcePosition)
    }

    companion object {
        @JvmStatic
        val CODEC: Codec<AttackedData> = RecordCodecBuilder.create {
            it.group(
                Codec.FLOAT.fieldOf("value").forGetter { it.damageValue },
                DamageSourceData.CODEC.fieldOf("damage_source").forGetter { it.damageSourceData },
                FreeCollisionBox.CODEC.fieldOf("box").forGetter { it.damageBox },
                Codec.STRING.optionalFieldOf("damage_bone").forGetter { Optional.ofNullable(it.damageBone) }
            ).apply(it) { v, s, b ,b2 ->
                AttackedData(v, s, b, b2.orElse(null))
            }
        }

        @JvmStatic
        val STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, AttackedData> {
            override fun decode(buffer: RegistryFriendlyByteBuf): AttackedData {
                val value = buffer.readFloat()
                val data = DamageSourceData.STREAM_CODEC.decode(buffer)
                val box = FreeCollisionBox.STREAM_CODEC.decode(buffer)
                val hitBone = buffer.readNullable(ByteBufCodecs.STRING_UTF8)
                return AttackedData(value, data, box, hitBone)
            }

            override fun encode(buffer: RegistryFriendlyByteBuf, value: AttackedData) {
                buffer.writeFloat(value.damageValue)
                DamageSourceData.STREAM_CODEC.encode(buffer, value.damageSourceData)
                FreeCollisionBox.STREAM_CODEC.encode(buffer, value.damageBox)
                buffer.writeNullable(value.damageBone, ByteBufCodecs.STRING_UTF8)
            }
        }

        @JvmStatic
        val LIST_CODEC = Codec.list(CODEC).xmap(
            { ArrayList(it) },
            { it.toList() }
        )


        @JvmStatic
        val LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.collection { arrayListOf() })
    }

}