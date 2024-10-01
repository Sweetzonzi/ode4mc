package cn.solarmoon.spark_core.api.animation.anim

import cn.solarmoon.spark_core.api.animation.anim.part.Animation
import cn.solarmoon.spark_core.api.animation.model.CommonModel
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.EntityType
import java.util.Optional

/**
 * 保存了客户端渲染完整动画和模型所需的必要数据
 */
data class ClientAnimData(
    var id: Int,
    var defaultAnim: Animation?,
    var tick: Int,
    var maxTick: Int,
    var presentAnim: Animation?,
    var lifeTime: Double,
    var transitionType: TransitionType,
    var transTick: Int,
    var maxTransTick: Int,
    var targetAnim: Animation?,
    var model: CommonModel,
    var animationSet: AnimationSet
) {
    
    fun copy(): ClientAnimData {
        return ClientAnimData(id, defaultAnim?.copy(), tick, maxTick, presentAnim?.copy(), lifeTime, transitionType, transTick, maxTransTick, targetAnim?.copy(), model.copy(), animationSet.copy())
    }

    companion object {
        @JvmStatic
        val STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, ClientAnimData> {
            override fun decode(buffer: RegistryFriendlyByteBuf): ClientAnimData {
                val id = buffer.readInt()
                val defaultAnim = readAnim(buffer)
                val tick = buffer.readInt()
                val maxTick = buffer.readInt()
                val presentAnim = readAnim(buffer)
                val lifeTime = buffer.readDouble()
                val transitionType = buffer.readEnum(TransitionType::class.java)
                val transTick = buffer.readInt()
                val maxTransTick = buffer.readInt()
                val targetAnim = readAnim(buffer)
                val model = CommonModel.STREAM_CODEC.decode(buffer)
                val animationSet = AnimationSet.STREAM_CODEC.decode(buffer)
                return ClientAnimData(id, defaultAnim, tick, maxTick, presentAnim, lifeTime, transitionType, transTick, maxTransTick, targetAnim, model, animationSet)
            }

            override fun encode(buffer: RegistryFriendlyByteBuf, value: ClientAnimData) {
                buffer.writeInt(value.id)
                writeAnim(buffer, value.defaultAnim)
                buffer.writeInt(value.tick)
                buffer.writeInt(value.maxTick)
                writeAnim(buffer, value.presentAnim)
                buffer.writeDouble(value.lifeTime)
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
        val CODEC: Codec<ClientAnimData> = RecordCodecBuilder.create {
            it.group(
                Codec.INT.fieldOf("id").forGetter { it.id },
                Animation.CODEC.optionalFieldOf("default_animation").forGetter { Optional.ofNullable(it.defaultAnim) },
                Codec.INT.fieldOf("tick").forGetter { it.tick },
                Codec.INT.fieldOf("max_tick").forGetter { it.maxTick },
                Animation.CODEC.optionalFieldOf("present_animation").forGetter { Optional.ofNullable(it.presentAnim) },
                Codec.DOUBLE.fieldOf("life_time").forGetter { it.lifeTime },
                Codec.STRING.fieldOf("transition").forGetter { it.transitionType.toString().lowercase() },
                Codec.INT.fieldOf("transtick").forGetter { it.transTick },
                Codec.INT.fieldOf("max_transtick").forGetter { it.maxTransTick },
                Animation.CODEC.optionalFieldOf("target_animation").forGetter { Optional.ofNullable(it.targetAnim) },
                CommonModel.CODEC.fieldOf("model").forGetter { it.model },
                AnimationSet.CODEC.fieldOf("animation_set").forGetter { it.animationSet }
            ).apply(it) { i, aa, a, b, c, s, f, t1, t2, t3, d, e ->
                ClientAnimData(i, aa.orElse(null), a, b, c.orElse(null), s, TransitionType.valueOf(f.uppercase()), t1, t2, t3.orElse(null), d, e)
            }
        }

        /**
         * 创建一个全新的data，预设了一些无挂紧要的参数，省去一些麻烦
         */
        @JvmStatic
        fun create(id: Int, type: EntityType<*>) = ClientAnimData(id, null, 0, 0, null, 0.0, TransitionType.LINEAR, 0, 5, null, CommonModel.getOriginCopy(type), AnimationSet.getOriginCopy(type))

        @JvmStatic
        val EMPTY = ClientAnimData(0, null, 0, 0, null, 0.0, TransitionType.LINEAR, 0, 0, null, CommonModel(0, 0, arrayListOf()), AnimationSet(arrayListOf()))
    }

}