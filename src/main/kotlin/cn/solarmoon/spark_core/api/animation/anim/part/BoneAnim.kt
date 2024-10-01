package cn.solarmoon.spark_core.api.animation.anim.part

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.anim.ClientAnimData
import cn.solarmoon.spark_core.api.data.SerializeHelper.VECTOR3F_CODEC
import cn.solarmoon.spark_core.api.data.SerializeHelper.VECTOR3F_STREAM_CODEC
import cn.solarmoon.spark_core.api.animation.anim.TransitionType
import cn.solarmoon.spark_core.api.animation.anim.IAnimatable
import cn.solarmoon.spark_core.api.phys.toDegrees
import cn.solarmoon.spark_core.api.phys.toRadians
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import org.joml.Vector3f
import thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d.toVec3
import java.util.TreeMap

/**
 * 骨骼动画，是一个完整动画的一部分，是动画的最小单位，受animation制约和控制
 */
data class BoneAnim(
    val name: String,
    val baseLifeTime: Double,
    val rotationSequence: TreeMap<Double, Vector3f>,
    val positionSequence: TreeMap<Double, Vector3f>
) {

    /**
     * 当前骨骼动画所从属的动画，会在动画加载时赋值，因此只要在世界内调用无需担心为null
     */
    var rootAnimation: Animation? = null

    var posCache: Vector3f? = null
    var rotCache: Vector3f? = null

    /**
     * 获取当前绑定实体在当前动画的当前tick所对应的旋转值
     * @return 进行过基础偏移后的旋转弧度角
     */
    fun getPresentRot(animData: ClientAnimData, partialTick: Float = 0F): Vector3f {
        val time = (animData.tick + partialTick) / 20f
        // 当前时间和基础时间的比值，用于缩放时间点
        val timeScale = animData.lifeTime / baseLifeTime
        // 在各个时间内两两遍历，定位到当前间隔进行变换
        rotationSequence.entries.zipWithNext().forEach { (a, b) ->
            val timestampA = a.key * timeScale; val aRot = a.value.toRadians()
            val timestampB = b.key * timeScale; val bRot = b.value.toRadians()
            if (time > timestampA && time <= timestampB) {
                val timeInternal = timestampB - timestampA
                val progress = (time - timestampA) / timeInternal
                val rot = animData.transitionType.lerpRot(progress, aRot, bRot).apply { x = -x; y = -y }
                return rot
            }
        }
        // 并且要考虑map只有一个元素的情况
        val presentBoneAnimRotOfThis = rotationSequence.lastEntry()?.value
        if (presentBoneAnimRotOfThis != null) {
            return presentBoneAnimRotOfThis.toRadians().apply { x = -x; y = -y }
        }
        var originRot = animData.model.getBone(name).rotationInRadians.toVector3f()
        return originRot
    }

    /**
     * 计算指定时间的变换，这里旋转是直接修改数值所以无所谓在哪个域
     *
     * @return 返回计算后的旋转值，在对应骨骼处应用
     */
    fun applyRotTransform(animData: ClientAnimData, partialTick: Float = 0F): Vector3f {
        val presentRot = if (animData.targetAnim == null || rotCache == null) getPresentRot(animData, partialTick) else rotCache!!
        var rot = Vector3f(presentRot)

        // 如果存在过渡目标，则获取过度目标的开始变换，然后使用插值过渡当前到目标
        animData.targetAnim?.let { t ->
            val it = t.getBoneAnim(name)?.rotationSequence[0.0] ?: animData.model.getBone(name).rotation.toVector3f() // 原始位置和动画位置哪个有选哪个
            val tRot = it.toRadians().apply { x = -x; y = -y }
            val time = animData.transTick + partialTick
            val progress = time / animData.maxTransTick.toDouble()
            rot = animData.transitionType.lerpValue(progress, presentRot, tRot)
        }

        // 在过渡动画开始的一瞬间对当前动画位置进行缓存，并使用此缓存进行过渡
        if (animData.targetAnim != null && rotCache == null ) {
            rotCache = Vector3f(rot)
        } else if (animData.targetAnim == null) {
            rotCache = null
        }

        return rot
    }

    /**
     * **警告：我不知道是什么导致了这样，但是json文件中各个时间点pos值在相等的情况下在动画末尾会突变为0，因此如果一个骨骼在同一个动画中始终保持相同的位置，那就不要设置多个位置点了**
     * @return 获取指定tick位置的位移数值，如果不在任何区间内，返回第一个位置
     */
    fun getPresentPos(animData: ClientAnimData, partialTick: Float = 0F): Vector3f {
        val time = (animData.tick + partialTick) / 20f
        // 当前时间和基础时间的比值，用于缩放时间点
        val timeScale = animData.lifeTime / baseLifeTime
        // 在各个时间内两两遍历，定位到当前间隔进行变换
        positionSequence.entries.zipWithNext().forEach { (a, b) ->
            val timestampA = a.key * timeScale; val aPos = a.value
            val timestampB = b.key * timeScale; val bPos = b.value
            if (time >= timestampA && time < timestampB) {
                val timeInternal = timestampB - timestampA
                val progress = (time - timestampA) / timeInternal
                val pos = animData.transitionType.lerpValue(progress, aPos, bPos).div(16f)
                return Vector3f(-pos.x, pos.y, pos.z)
            }
        }
        // 并且要考虑map只有一个元素的情况
        val presentBoneAnimPosOfThis = positionSequence.lastEntry()?.value
        if (presentBoneAnimPosOfThis != null) {
            return presentBoneAnimPosOfThis.div(16f).apply { x = -x }
        }
        return Vector3f()
    }

    /**
     * 应用当前所绑定实体的动画时间所对应的动画位移变换到给定域
     */
    fun applyPosTransform(animData: ClientAnimData, matrix: Matrix4f, partialTick: Float = 0F) {
        val presentPos = if (animData.targetAnim == null || posCache == null) getPresentPos(animData, partialTick) else posCache!!
        var pos = Vector3f(presentPos)

        // 如果存在过渡目标，则获取过度目标的开始变换，然后使用插值过渡当前到目标
        animData.targetAnim?.let { t ->
            val it = t.getBoneAnim(name)?.positionSequence[0.0] ?: Vector3f() // 原始位置和动画位置哪个有选哪个
            val tPos = it.div(16f).apply { x = -x }
            val time = animData.transTick + partialTick
            val progress = time / animData.maxTransTick.toDouble()
            pos = animData.transitionType.lerpValue(progress, presentPos, tPos)
        }

        // 在过渡动画开始的一瞬间对当前动画位置进行缓存，并使用此缓存进行过渡
        if (animData.targetAnim != null && posCache == null ) {
            posCache = Vector3f(pos)
        } else if (animData.targetAnim == null) {
            posCache = null
        }

        matrix.translate(pos.x, pos.y, pos.z)
    }

    fun copy(): BoneAnim {
        val copiedRotationSequence = TreeMap<Double, Vector3f>()
        rotationSequence.forEach { (key, value) -> copiedRotationSequence[key] = Vector3f(value) }

        val copiedPositionSequence = TreeMap<Double, Vector3f>()
        positionSequence.forEach { (key, value) -> copiedPositionSequence[key] = Vector3f(value) }

        return BoneAnim(name, baseLifeTime, copiedRotationSequence, copiedPositionSequence).also {
            it.rootAnimation = this.rootAnimation
        }
    }

    companion object {
        @JvmStatic
        val LINKED_HASH_MAP_CODEC: Codec<TreeMap<String, Vector3f>> = Codec.unboundedMap(Codec.STRING, VECTOR3F_CODEC).xmap( { TreeMap(it) }, { it } )

        @JvmStatic
        val LINKED_HASH_MAP_STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, TreeMap<Double, Vector3f>> {
            override fun encode(output: RegistryFriendlyByteBuf, value: TreeMap<Double, Vector3f>) {
                output.writeInt(value.size)
                for ((key, vector) in value) {
                    ByteBufCodecs.DOUBLE.encode(output, key)
                    VECTOR3F_STREAM_CODEC.encode(output, vector)
                }
            }

            override fun decode(input: RegistryFriendlyByteBuf): TreeMap<Double, Vector3f> {
                val size = input.readInt()
                val map = TreeMap<Double, Vector3f>()
                repeat(size) {
                    val key = ByteBufCodecs.DOUBLE.decode(input)
                    val vector = VECTOR3F_STREAM_CODEC.decode(input)
                    map[key] = vector
                }
                return map
            }
        }

        @JvmStatic
        val CODEC: Codec<BoneAnim> = RecordCodecBuilder.create {
            it.group(
                Codec.STRING.fieldOf("name").forGetter { it.name },
                Codec.DOUBLE.fieldOf("base_lifetime").forGetter { it.baseLifeTime },
                LINKED_HASH_MAP_CODEC.fieldOf("rotation_sequence").forGetter {
                    val tmap = it.rotationSequence
                    val map = TreeMap<String, Vector3f>()
                    for ((d, e) in tmap) {
                        map.put(d.toString(), e)
                    }
                    map
                                                                             },
                LINKED_HASH_MAP_CODEC.fieldOf("position_sequence").forGetter {
                    val tmap = it.positionSequence
                    val map = TreeMap<String, Vector3f>()
                    for ((d, e) in tmap) {
                        map.put(d.toString(), e)
                    }
                    map
                }
            ).apply(it) { a, b, c, d ->
                val map1 = TreeMap<Double, Vector3f>()
                val map2 = TreeMap<Double, Vector3f>()
                for ((s, e) in c) {
                    map1.put(s.toDouble(), e)
                }
                for ((s, e) in d) {
                    map2.put(s.toDouble(), e)
                }
                BoneAnim(a, b, map1, map2)
            }
        }

        @JvmStatic
        val LIST_CODEC = CODEC.listOf()

        @JvmStatic
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, BoneAnim::name,
            ByteBufCodecs.DOUBLE, BoneAnim::baseLifeTime,
            LINKED_HASH_MAP_STREAM_CODEC, BoneAnim::rotationSequence,
            LINKED_HASH_MAP_STREAM_CODEC, BoneAnim::positionSequence,
            ::BoneAnim
        )

        @JvmStatic
        val LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.collection { arrayListOf() })
    }

}
