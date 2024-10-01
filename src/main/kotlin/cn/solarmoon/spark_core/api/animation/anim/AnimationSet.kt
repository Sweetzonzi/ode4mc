package cn.solarmoon.spark_core.api.animation.anim

import cn.solarmoon.spark_core.api.animation.anim.part.Animation
import cn.solarmoon.spark_core.api.animation.model.CommonModel
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType

/**
 * 动画集合，代表一个模型的所有动画
 */
data class AnimationSet(
    val animations: ArrayList<Animation>
) {

    /**
     * 获取第一个匹配输入名字的动画
     */
    fun getAnimation(name: String): Animation {
        return animations.filter { it.name == name }.first()
    }

    fun copy(): AnimationSet {
        val list = arrayListOf<Animation>()
        animations.forEach { list.add(it.copy()) }
        return AnimationSet(list)
    }

    companion object {
        /**
         * 安全获取原始数据，目的是保证原始数据不被修改，并应用生物数据
         * @param type 如果实体已经初始化，无需传递type，但是如果实体还未初始化，则必须传递type
         */
        @JvmStatic
        fun getOriginCopy(type: EntityType<*>) = ORIGINS[BuiltInRegistries.ENTITY_TYPE.getKey(type)]?.copy() ?: AnimationSet(arrayListOf())

        @JvmStatic
        val ORIGINS = mutableMapOf<ResourceLocation, AnimationSet>()

        @JvmStatic
        val ORIGIN_MAP_STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, MutableMap<ResourceLocation, AnimationSet>> {
            override fun decode(buffer: RegistryFriendlyByteBuf): MutableMap<ResourceLocation, AnimationSet> {
                val map = mutableMapOf<ResourceLocation, AnimationSet>()
                val size = buffer.readInt()
                repeat(size) {
                    val id = buffer.readResourceLocation()
                    val anim = STREAM_CODEC.decode(buffer)
                    map.put(id, anim)
                }
                return map
            }

            override fun encode(buffer: RegistryFriendlyByteBuf, value: MutableMap<ResourceLocation, AnimationSet>) {
                buffer.writeInt(value.size)
                value.forEach { id, anim ->
                    buffer.writeResourceLocation(id)
                    STREAM_CODEC.encode(buffer, anim)
                }
            }
        }

        @JvmStatic
        val CODEC: Codec<AnimationSet> = RecordCodecBuilder.create {
            it.group(
                Animation.LIST_CODEC.fieldOf("animations").forGetter { it.animations }
            ).apply(it) { AnimationSet(ArrayList(it)) }
        }

        @JvmStatic
        val STREAM_CODEC = StreamCodec.composite(
            Animation.LIST_STREAM_CODEC, AnimationSet::animations,
            ::AnimationSet
        )

        @JvmStatic
        val LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.collection { arrayListOf() })
    }

}
