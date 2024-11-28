package cn.solarmoon.spark_core.api.entity.attack

import cn.solarmoon.spark_core.api.phys.obb.OrientedBoundingBox
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import java.util.Optional

/**
 * 受击信息，不包含伤害源和伤害值，如果想调用这两个以检测直接找Entity的hurt方法插入即可
 * @param attacker 发起攻击的实体，具体是箭还是说射箭的生物，需要看自己的攻击逻辑而定
 * @param damageBox 触发该次攻击的box，可以通过box大小位置等信息实现想要的效果
 * @param damageBone 该次攻击的box所击中的骨骼
 * @param extraData 可以附加额外数据到此次攻击
 */
data class AttackedData(
    val attacker: Int,
    val damageBox: OrientedBoundingBox,
    val damageBone: String?,
    val extraData: CompoundTag = CompoundTag()
) {

    /**
     * 当将此值设为true时，将在生物的下一个tick删除该数据，以免多次调用该数据后对应的方法
     */
    var isCancelled = false

    fun getAttacker(level: Level) = level.getEntity(attacker)

    companion object {
        @JvmStatic
        val CODEC: Codec<AttackedData> = RecordCodecBuilder.create {
            it.group(
                Codec.INT.fieldOf("attacker").forGetter { it.attacker },
                OrientedBoundingBox.CODEC.fieldOf("box").forGetter { it.damageBox },
                Codec.STRING.optionalFieldOf("damage_bone").forGetter { Optional.ofNullable(it.damageBone) },
                CompoundTag.CODEC.fieldOf("extra_data").forGetter { it.extraData }
            ).apply(it) { v, b ,b2, n ->
                AttackedData(v, b, b2.orElse(null), n)
            }
        }

        @JvmStatic
        val OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC)
    }

}