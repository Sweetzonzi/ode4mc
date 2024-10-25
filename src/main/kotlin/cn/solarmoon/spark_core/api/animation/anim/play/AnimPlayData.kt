package cn.solarmoon.spark_core.api.animation.anim.play

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.anim.part.Animation
import cn.solarmoon.spark_core.api.phys.copy
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.codec.StreamCodec
import org.joml.Matrix4f
import org.joml.Vector3f
import thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d.toVec3

data class AnimPlayData(
    var mixedAnimations: MutableSet<MixedAnimation>
) {

    fun getMixedBoneAnimRotation(boneName: String, partialTick: Float = 0f): Vector3f {
        val mixed = Vector3f()
        mixedAnimations.forEach {
            val weight = getMixedWeight(it, partialTick)
            it.animation.getBoneAnim(boneName)?.getPresentAnimRot(it, partialTick)?.let { rot ->
                mixed.add(rot.mul(weight))
            }
        }
        return mixed
    }

    fun getMixedBoneAnimPosition(boneName: String, partialTick: Float = 0f): Vector3f {
        val mixed = Vector3f()
        mixedAnimations.forEach {
            val weight = getMixedWeight(it, partialTick)
            it.animation.getBoneAnim(boneName)?.getPresentAnimPos(it, partialTick)?.let { pos ->
                mixed.add(pos.mul(weight))
            }
        }
        return mixed
    }

    fun getMixedBoneAnimScale(boneName: String, partialTick: Float = 0f): Vector3f {
        var mixed = Vector3f(1f)
        var totalWeight = 0f

        mixedAnimations.forEach {
            val weight = getMixedWeight(it, partialTick).takeIf { it > 0 } ?: return@forEach
            it.animation.getBoneAnim(boneName)?.getPresentAnimScale(it, partialTick)?.let { scale ->
                // 混合比例
                val mixRatio = weight / (weight + totalWeight)
                mixed.lerp(scale, mixRatio)
                totalWeight += weight
            }
        }

        return mixed
    }

    /**
     * 获取混合动画中指定动画的权重
     */
    fun getMixedWeight(mixedAnimation: MixedAnimation, partialTick: Float = 0f): Float {
        val totalWeight = mixedAnimations.sumOf { (it.getWeight(partialTick)).toDouble() }
        var weight = mixedAnimation.getWeight(partialTick)
        return (
                if (mixedAnimations.size == 1) weight
                else if (totalWeight > 0) weight.toFloat() / totalWeight.toFloat()
                else 0f
                ).coerceIn(0f, 1f)
    }

    fun getMixedAnimations(name: String): List<MixedAnimation> = mixedAnimations.filter { it.animation.name == name }.toList()

    fun copy(): AnimPlayData {
        val set = mutableSetOf<MixedAnimation>()
        mixedAnimations.forEach { set.add(it.copy()) }
        return AnimPlayData(set)
    }

    companion object {
        @JvmStatic
        val CODEC: Codec<AnimPlayData> = RecordCodecBuilder.create {
            it.group(
                MixedAnimation.LIST_CODEC.fieldOf("mixed_animations").forGetter { it.mixedAnimations }
            ).apply(it, ::AnimPlayData)
        }

        @JvmStatic
        val STREAM_CODEC = StreamCodec.composite(
            MixedAnimation.LIST_STREAM_CODEC, AnimPlayData::mixedAnimations,
            ::AnimPlayData
        )

        @JvmStatic
        val EMPTY get() = AnimPlayData(mutableSetOf())
    }

}
