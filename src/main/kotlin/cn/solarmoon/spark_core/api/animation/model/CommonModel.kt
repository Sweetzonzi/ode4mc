package cn.solarmoon.spark_core.api.animation.model

import cn.solarmoon.spark_core.api.animation.anim.ClientAnimData
import cn.solarmoon.spark_core.api.animation.anim.part.Animation
import cn.solarmoon.spark_core.api.animation.model.part.BonePart
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import org.joml.Matrix3f
import org.joml.Matrix4f
import kotlin.collections.mutableMapOf

/**
 * 以服务端为根基的模型数据，客户端只能调用，不可以试图在客户端修改！
 */
data class CommonModel(
    val textureWidth: Int,
    val textureHeight: Int,
    val bones: ArrayList<BonePart>
) {

    init {
        bones.forEach { it.rootModel = this }
    }

    /**
     * 根据名字获取特定骨骼组，名字在json文件中定义，如果重名只会获得第一个骨骼
     */
    fun getBone(name: String): BonePart {
        return bones.first { it.name == name }
    }

    /**
     * @param normal3f 法线的矩阵，从当前poseStack获取
     */
    @OnlyIn(Dist.CLIENT)
    fun renderBones(animData: ClientAnimData, matrix4f: Matrix4f, normal3f: Matrix3f, buffer: VertexConsumer, packedLight: Int, packedOverlay: Int, color: Int, partialTick: Float = 0f) {
        bones.forEach {
            it.renderCubes(animData, Matrix4f(matrix4f), normal3f, buffer, packedLight, packedOverlay, color, partialTick)
        }
    }

    fun copy(): CommonModel {
        val copiedBones = ArrayList<BonePart>()
        bones.forEach { copiedBones.add(it.copy()) }
        return CommonModel(textureWidth, textureHeight, copiedBones)
    }

    companion object {
        /**
         * 安全获取原始数据，目的是保证原始数据不被修改
         */
        @JvmStatic
        fun getOriginCopy(entityType: EntityType<*>) = ORIGINS[BuiltInRegistries.ENTITY_TYPE.getKey(entityType)]?.copy() ?: CommonModel(0, 0, arrayListOf())

        /**
         * 地图加载后读取的原始模型数据，最好不要修改
         */
        @JvmStatic
        val ORIGINS = mutableMapOf<ResourceLocation, CommonModel>()

        @JvmStatic
        val ORIGIN_MAP_STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, MutableMap<ResourceLocation, CommonModel>> {
            override fun decode(buffer: RegistryFriendlyByteBuf): MutableMap<ResourceLocation, CommonModel> {
                val map = mutableMapOf<ResourceLocation, CommonModel>()
                val size = buffer.readInt()
                repeat(size) {
                    val id = buffer.readResourceLocation()
                    val model = STREAM_CODEC.decode(buffer)
                    map.put(id, model)
                }
                return map
            }

            override fun encode(buffer: RegistryFriendlyByteBuf, value: MutableMap<ResourceLocation, CommonModel>) {
                buffer.writeInt(value.size)
                value.forEach { id, model ->
                    buffer.writeResourceLocation(id)
                    STREAM_CODEC.encode(buffer, model)
                }
            }
        }

        @JvmStatic
        val CODEC: Codec<CommonModel> = RecordCodecBuilder.create {
            it.group(
                Codec.INT.fieldOf("textureWidth").forGetter { it.textureWidth },
                Codec.INT.fieldOf("textureHeight").forGetter { it.textureHeight },
                BonePart.LIST_CODEC.fieldOf("bones").forGetter { it.bones }
            ).apply(it) { a, b, c -> CommonModel(a, b, ArrayList(c)) }
        }

        @JvmStatic
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, CommonModel::textureWidth,
            ByteBufCodecs.INT, CommonModel::textureHeight,
            BonePart.LIST_STREAM_CODEC, CommonModel::bones,
            ::CommonModel
        )
    }

}