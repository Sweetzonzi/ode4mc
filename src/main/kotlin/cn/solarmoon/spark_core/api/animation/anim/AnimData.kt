package cn.solarmoon.spark_core.api.animation.anim

import cn.solarmoon.spark_core.api.animation.anim.part.Animation
import cn.solarmoon.spark_core.api.animation.model.CommonModel
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.EntityType
import java.util.Optional
import kotlin.math.ceil

/**
 * 保存了客户端渲染完整动画和模型所需的必要数据
 */
data class AnimData(//
    var id: Int,
    var defaultAnim: Animation?,
    var tick: Int,
    var lifeTime: Double,
    var presentAnim: Animation?,
    var transitionType: TransitionType,
    var transTick: Int,
    var maxTransTick: Int,
    var targetAnim: Animation?,
    var model: CommonModel,
    var animationSet: AnimationSet
) {

    val maxTick
        get() = ceil(lifeTime * 20).toInt()
    
    fun copy(): AnimData {
        return AnimData(id, defaultAnim?.copy(), tick, lifeTime, presentAnim?.copy(), transitionType, transTick, maxTransTick, targetAnim?.copy(), model.copy(), animationSet.copy())
    }

    companion object {
        @JvmStatic
        val STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, AnimData> {
            override fun decode(buffer: RegistryFriendlyByteBuf): AnimData {
                val id = buffer.readInt()
                val defaultAnim = readAnim(buffer)
                val tick = buffer.readInt()
                val lifeTime = buffer.readDouble()
                val presentAnim = readAnim(buffer)
                val transitionType = buffer.readEnum(TransitionType::class.java)
                val transTick = buffer.readInt()
                val maxTransTick = buffer.readInt()
                val targetAnim = readAnim(buffer)
                val model = CommonModel.STREAM_CODEC.decode(buffer)
                val animationSet = AnimationSet.STREAM_CODEC.decode(buffer)
                return AnimData(id, defaultAnim, tick, lifeTime, presentAnim, transitionType, transTick, maxTransTick, targetAnim, model, animationSet)
            }

            override fun encode(buffer: RegistryFriendlyByteBuf, value: AnimData) {
                buffer.writeInt(value.id)
                writeAnim(buffer, value.defaultAnim)
                buffer.writeInt(value.tick)
                buffer.writeDouble(value.lifeTime)
                writeAnim(buffer, value.presentAnim)
                buffer.writeEnum(value.transitionType)
                buffer.writeInt(value.transTick)
                buffer.writeInt(value.maxTransTick)
                writeAnim(buffer, value.targetAnim)
                CommonModel.STREAM_CODEC.encode(buffer, value.model)
                AnimationSet.STREAM_CODEC.encode(buffer, value.animationSet)
            }

            fun writeAnim(buffer: RegistryFriendlyByteBuf, anim: Animation?) {
                if (anim != null) {
                    buffer.writeBoolean(true)
                    Animation.STREAM_CODEC.encode(buffer, anim)
                } else {
                    buffer.writeBoolean(false)
                }
            }

            fun readAnim(buffer: RegistryFriendlyByteBuf): Animation? {
                return if (buffer.readBoolean()) Animation.STREAM_CODEC.decode(buffer) else null
            }
        }

        @JvmStatic
        val CODEC: Codec<AnimData> = RecordCodecBuilder.create {
            it.group(
                Codec.INT.fieldOf("id").forGetter { it.id },
                Animation.CODEC.optionalFieldOf("default_animation").forGetter { Optional.ofNullable(it.defaultAnim) },
                Codec.INT.fieldOf("tick").forGetter { it.tick },
                Codec.DOUBLE.fieldOf("life_time").forGetter { it.lifeTime },
                Animation.CODEC.optionalFieldOf("present_animation").forGetter { Optional.ofNullable(it.presentAnim) },
                Codec.STRING.fieldOf("transition").forGetter { it.transitionType.toString().lowercase() },
                Codec.INT.fieldOf("transtick").forGetter { it.transTick },
                Codec.INT.fieldOf("max_transtick").forGetter { it.maxTransTick },
                Animation.CODEC.optionalFieldOf("target_animation").forGetter { Optional.ofNullable(it.targetAnim) },
                CommonModel.CODEC.fieldOf("model").forGetter { it.model },
                AnimationSet.CODEC.fieldOf("animation_set").forGetter { it.animationSet }
            ).apply(it) { i, aa, a, s, c, f, t1, t2, t3, d, e ->
                AnimData(i, aa.orElse(null), a, s, c.orElse(null), TransitionType.valueOf(f.uppercase()), t1, t2, t3.orElse(null), d, e)
            }
        }

        /**
         * 创建一个全新的data，预设了一些无挂紧要的参数，省去一些麻烦
         */
        @JvmStatic
        fun create(id: Int, type: EntityType<*>) = AnimData(id, null, 0, 0.0, null, TransitionType.LINEAR, 0, 5, null, CommonModel.getOriginCopy(type), AnimationSet.getOriginCopy(type))

        @JvmStatic
        val EMPTY = AnimData(0, null, 0, 0.0, null, TransitionType.LINEAR, 0, 0, null, CommonModel(0, 0, arrayListOf()), AnimationSet(arrayListOf()))
    }

}