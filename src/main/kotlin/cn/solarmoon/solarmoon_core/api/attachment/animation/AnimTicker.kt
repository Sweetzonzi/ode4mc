package cn.solarmoon.solarmoon_core.api.attachment.animation

import com.mojang.serialization.Codec
import com.mojang.serialization.Dynamic
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.world.level.block.entity.BlockEntity
import net.neoforged.neoforge.common.util.INBTSerializable
import net.neoforged.neoforge.fluids.FluidStack

/**
 * 动画计时器，能够存储各类动画信息
 *
 * 必须提醒的是，不可在客户端设置ticker的值，会导致各种各样的问题，最优是在服务端自上而下地让客户端接收数据
 */
data class AnimTicker(
    val fixedValues: MutableMap<String, Float> = mutableMapOf(),
    val timers: MutableMap<String, Timer> = mutableMapOf(),
    var fixedElements: MutableMap<String, CompoundTag> = mutableMapOf()
) {

    companion object {
        @JvmStatic
        val CODEC: Codec<AnimTicker> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.unboundedMap(Codec.STRING, Codec.FLOAT).xmap(
                    { it.toMutableMap() }, // 重要！转换为可变的 Map
                    { it }
                ).fieldOf("fixedValues").forGetter { it.fixedValues },
                Codec.unboundedMap(Codec.STRING, Timer.CODEC).xmap(
                    { it.toMutableMap() },
                    { it }
                ).fieldOf("timers").forGetter { it.timers },
                Codec.unboundedMap(Codec.STRING, CompoundTag.CODEC).xmap(
                    { it.toMutableMap() },
                    { it }
                ).fieldOf("fixedElements").forGetter { it.fixedElements }
            ).apply(instance, ::AnimTicker)
        }
    }

}