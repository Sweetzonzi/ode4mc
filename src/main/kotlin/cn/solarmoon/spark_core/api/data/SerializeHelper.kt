package cn.solarmoon.spark_core.api.data

import com.mojang.serialization.Codec
import net.minecraft.Util
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import org.joml.Vector3f

/**
 * 补充一些解码方法
 */
object SerializeHelper {

    @JvmStatic
    val BLOCK_CODEC: Codec<Block> = Codec.STRING.xmap({ BuiltInRegistries.BLOCK.get(ResourceLocation.parse(it)) }, { BuiltInRegistries.BLOCK.getKey(it).toString() })

    @JvmStatic
    val BLOCK_STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, Block> {
        override fun decode(buffer: RegistryFriendlyByteBuf): Block {
            return BuiltInRegistries.BLOCK.get(buffer.readResourceLocation())
        }

        override fun encode(buffer: RegistryFriendlyByteBuf, value: Block) {
            buffer.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(value))
        }
    }

    @JvmStatic
    val FLUID_CODEC: Codec<Fluid> = Codec.STRING.xmap({ BuiltInRegistries.FLUID.get(ResourceLocation.parse(it)) }, { BuiltInRegistries.FLUID.getKey(it).toString() })

    @JvmStatic
    val FLUID_STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, Fluid> {
        override fun decode(buffer: RegistryFriendlyByteBuf): Fluid {
            return BuiltInRegistries.FLUID.get(buffer.readResourceLocation())
        }

        override fun encode(buffer: RegistryFriendlyByteBuf, value: Fluid) {
            buffer.writeResourceLocation(BuiltInRegistries.FLUID.getKey(value))
        }
    }

    @JvmStatic
    val ITEMSTACK_LIST_CODEC = ItemStack.CODEC.listOf()

    @JvmStatic
    val ITEMSTACK_OPTIONAL_LIST_CODEC = ItemStack.OPTIONAL_CODEC.listOf()

    object INGREDIENT {
        @JvmStatic
        val LIST_STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, List<Ingredient>> = Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.collection { mutableListOf() })
    }

    @JvmStatic
    val VEC2_CODEC: Codec<Vec2> = Codec.FLOAT.listOf().comapFlatMap(
        { Util.fixedSize(it, 2).map { Vec2(it[0], it[1]) } },
        { listOf(it.x, it.y) }
    )

    @JvmStatic
    val VECTOR3F_CODEC: Codec<Vector3f> = Codec.FLOAT.listOf().xmap(
        { list -> Vector3f(list[0], list[1], list[2]) },
        { vec -> listOf(vec.x, vec.y, vec.z) }
    )

    @JvmStatic
    val VECTOR3F_STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, Vector3f> {
        override fun decode(buffer: RegistryFriendlyByteBuf): Vector3f {
            return Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat())
        }

        override fun encode(buffer: RegistryFriendlyByteBuf, value: Vector3f) {
            buffer.writeFloat(value.x)
            buffer.writeFloat(value.y)
            buffer.writeFloat(value.z)
        }
    }

    @JvmStatic
    val VEC3_STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, Vec3> {
        override fun decode(buffer: RegistryFriendlyByteBuf): Vec3 {
            val x = buffer.readDouble()
            val y = buffer.readDouble()
            val z = buffer.readDouble()
            return Vec3(x, y, z)
        }

        override fun encode(buffer: RegistryFriendlyByteBuf, value: Vec3) {
            buffer.writeDouble(value.x)
            buffer.writeDouble(value.y)
            buffer.writeDouble(value.z)
        }
    }

    @JvmStatic
    val VEC2_STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, Vec2> {
        override fun decode(buffer: RegistryFriendlyByteBuf): Vec2 {
            return Vec2(buffer.readFloat(), buffer.readFloat())
        }

        override fun encode(buffer: RegistryFriendlyByteBuf, value: Vec2) {
            buffer.writeFloat(value.x)
            buffer.writeFloat(value.y)
        }
    }

    @JvmStatic
    val QUATERNIONF_CODEC: Codec<Quaternionf> = Codec.FLOAT.listOf().xmap(
        { Quaternionf(it[0], it[1], it[2], it[3]) },
        { listOf(it.x, it.y, it.z, it.w) }
    )

    @JvmStatic
    val QUATERNIONF_STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, Quaternionf> {
        override fun decode(buffer: RegistryFriendlyByteBuf): Quaternionf {
            return Quaternionf(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat())
        }

        override fun encode(buffer: RegistryFriendlyByteBuf, value: Quaternionf) {
            buffer.writeFloat(value.x)
            buffer.writeFloat(value.y)
            buffer.writeFloat(value.z)
            buffer.writeFloat(value.w)
        }
    }

}