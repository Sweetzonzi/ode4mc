package cn.solarmoon.spark_core.api.animation.anim.play

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.anim.AnimationSet
import cn.solarmoon.spark_core.api.animation.anim.InterpolationType
import cn.solarmoon.spark_core.api.animation.anim.part.Animation
import cn.solarmoon.spark_core.api.animation.model.CommonModel
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import java.util.Optional
import kotlin.math.ceil

/**
 * 保存了客户端渲染完整动画和模型所需的必要数据
 *
 * 模型和动画路径格式如：minecraft:player 一般和该实体注册id一致
 */
data class AnimData(
    var modelPath: ResourceLocation,
    var animPath: ResourceLocation,
    val playData: AnimPlayData,
) {
    constructor(res: ResourceLocation, playData: AnimPlayData): this(res, res, playData)

    val model get() = CommonModel.get(modelPath)
    val animationSet get() = AnimationSet.get(animPath)
    val textureLocation get() = ResourceLocation.fromNamespaceAndPath(modelPath.namespace, "textures/entity/${modelPath.path}.png")

    fun copy(): AnimData {
        return AnimData(modelPath, animPath, playData.copy())
    }

    /**
     * 一键将模型和动画变为指定路径的模型和动画
     */
    fun changeTo(path: ResourceLocation) {
        modelPath = path
        animPath = path
    }

    companion object {
        @JvmStatic
        val STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, AnimData> {
            override fun decode(buffer: RegistryFriendlyByteBuf): AnimData {
                val model = buffer.readResourceLocation()
                val animations = buffer.readResourceLocation()
                val play = AnimPlayData.STREAM_CODEC.decode(buffer)
                return AnimData(model, animations, play)
            }

            override fun encode(buffer: RegistryFriendlyByteBuf, value: AnimData) {
                buffer.writeResourceLocation(value.modelPath)
                buffer.writeResourceLocation(value.animPath)
                AnimPlayData.STREAM_CODEC.encode(buffer, value.playData)
            }
        }

        @JvmStatic
        val CODEC: Codec<AnimData> = RecordCodecBuilder.create {
            it.group(
                ResourceLocation.CODEC.fieldOf("model_path").forGetter { it.modelPath },
                ResourceLocation.CODEC.fieldOf("anim_path").forGetter { it.animPath },
                AnimPlayData.CODEC.fieldOf("play").forGetter { it.playData },
            ).apply(it, ::AnimData)
        }

        @JvmStatic
        val EMPTY get() = AnimData(ResourceLocation.withDefaultNamespace("player"), ResourceLocation.withDefaultNamespace("player"), AnimPlayData.EMPTY)

        @JvmStatic
        fun of(entity: Entity): AnimData = AnimData(BuiltInRegistries.ENTITY_TYPE.getKey(entity.type), AnimPlayData.EMPTY)
    }

}