package cn.solarmoon.spark_core.api.phys.obb

import cn.solarmoon.spark_core.registry.common.SparkAttachments
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.Entity

/**
 * ### 可自主装载的obb，相当于可拆卸骨骼
 * - 当调用[cn.solarmoon.spark_core.api.entity.attack.AttackHelper]的攻击方法时，也会将这里的box考虑到检测范围
 */
data class MountableOBB(
    val type: Type,
    val box: OrientedBoundingBox
) {

    enum class Type {
        /**
         * 当[cn.solarmoon.spark_core.api.entity.attack.AttackHelper]中的boxAttack方法调用时，如果碰到了此类型的obb，则将首先返回此obb而非碰撞骨骼
         */
        STRIKABLE_BONE
    }

    companion object {
        @JvmStatic
        val CODEC: Codec<MountableOBB> = RecordCodecBuilder.create {
            it.group(
                Codec.STRING.fieldOf("type").forGetter { it.type.toString() },
                OrientedBoundingBox.CODEC.fieldOf("box").forGetter { it.box }
            ).apply(it) { type, box ->
                MountableOBB(Type.valueOf(type), box)
            }
        }

        @JvmStatic
        val STREAM_CODEC = object : StreamCodec<FriendlyByteBuf, MountableOBB> {
            override fun decode(buffer: FriendlyByteBuf): MountableOBB {
                val type = buffer.readEnum(Type::class.java)
                val box = OrientedBoundingBox.STREAM_CODEC.decode(buffer)
                return MountableOBB(type, box)
            }

            override fun encode(buffer: FriendlyByteBuf, value: MountableOBB) {
                buffer.writeEnum(value.type)
                OrientedBoundingBox.STREAM_CODEC.encode(buffer, value.box)
            }
        }

        @JvmStatic
        val MAP_CODEC = Codec.unboundedMap(Codec.STRING, CODEC).xmap(
            { it.toMutableMap() },
            { it.toMutableMap() }
        )
    }

}

fun Entity.setMountableOBB(id: String, box: MountableOBB) {
    getData(SparkAttachments.MOUNTABLE_OBB)[id] = box
}

fun Entity.clearMountableOBB(id: String) {
    getData(SparkAttachments.MOUNTABLE_OBB).remove(id)
}