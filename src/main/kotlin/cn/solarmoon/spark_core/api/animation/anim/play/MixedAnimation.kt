package cn.solarmoon.spark_core.api.animation.anim.play

import cn.solarmoon.spark_core.api.animation.anim.AnimationSet
import cn.solarmoon.spark_core.api.animation.anim.part.Animation
import cn.solarmoon.spark_core.api.entity.ai.attack.AttackedData
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import test.sp
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

data class MixedAnimation(
    var weight: Int,
    val animPath: String,
    val speed: Float,
    var tick: Double = 0.0,
    var transTick: Int = 0,
    var maxTransTick: Int = 5,
    var isCancelled: Boolean
) {
    constructor(modelPath: ResourceLocation, animName: String, weight: Int, speed: Float = 1f, maxTransTick: Int = 5):
            this(weight, "$modelPath:$animName", speed, maxTransTick = maxTransTick, isCancelled = false)

    val modelPath: ResourceLocation get() {
        val lastColonIndex = animPath.lastIndexOf(":")
        return ResourceLocation.parse(animPath.substring(0, lastColonIndex))
    }
    val animName: String get() {
        val lastColonIndex = animPath.lastIndexOf(":")
        return animPath.substring(lastColonIndex + 1)
    }
    val animation get() = AnimationSet.get(modelPath).getAnimation(animName)
    val maxTick get() = (animation.baseLifeTime / speed) * 20
    val isInTransition get() = isCancelled || transTick < maxTransTick

    /**
     * 获取带过渡的权重
     */
    fun getWeight(partialTicks: Float = 0f): Float = weight * getTransProgress(partialTicks)

    fun getTransProgress(partialTicks: Float = 0f): Float {
        var progress = 1f
        progress = if (isCancelled) (transTick - partialTicks) / maxTransTick
        else (transTick + partialTicks) / maxTransTick
        return progress.coerceIn(0f, 1f)
    }

    companion object {
        @JvmStatic
        val CODEC: Codec<MixedAnimation> = RecordCodecBuilder.create {
            it.group(
                Codec.INT.fieldOf("weight").forGetter { it.weight },
                Codec.STRING.fieldOf("anim_path").forGetter { it.animPath },
                Codec.FLOAT.fieldOf("speed").forGetter { it.speed },
                Codec.DOUBLE.fieldOf("tick").forGetter { it.tick },
                Codec.INT.fieldOf("transtick").forGetter { it.transTick },
                Codec.INT.fieldOf("max_transtick").forGetter { it.maxTransTick },
                Codec.BOOL.fieldOf("cancelled").forGetter { it.isCancelled },
            ).apply(it, ::MixedAnimation)
        }

        @JvmStatic
        val STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, MixedAnimation> {
            override fun decode(buffer: RegistryFriendlyByteBuf): MixedAnimation {
                val weight = buffer.readInt()
                val animation = buffer.readUtf()
                val speed = buffer.readFloat()
                val tick = buffer.readDouble()
                val transTick = buffer.readInt()
                val maxTransTick = buffer.readInt()
                val enabled = buffer.readBoolean()
                return MixedAnimation(weight, animation, speed, tick, transTick, maxTransTick, enabled)
            }

            override fun encode(buffer: RegistryFriendlyByteBuf, value: MixedAnimation) {
                buffer.writeInt(value.weight)
                buffer.writeUtf(value.animPath)
                buffer.writeFloat(value.speed)
                buffer.writeDouble(value.tick)
                buffer.writeInt(value.transTick)
                buffer.writeInt(value.maxTransTick)
                buffer.writeBoolean(value.isCancelled)
            }
        }

        @JvmStatic
        val LIST_CODEC = Codec.list(CODEC).xmap(
            { it.toMutableSet() },
            { it.toList() }
        )

        @JvmStatic
        val LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.collection { mutableSetOf() })
    }

}
