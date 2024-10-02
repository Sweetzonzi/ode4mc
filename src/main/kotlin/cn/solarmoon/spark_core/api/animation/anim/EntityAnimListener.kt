package cn.solarmoon.spark_core.api.animation.anim

import cn.solarmoon.spark_core.api.animation.anim.part.Animation
import cn.solarmoon.spark_core.api.animation.anim.part.BoneAnim
import cn.solarmoon.spark_core.api.data.SimpleJsonListener
import com.google.gson.JsonElement
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.GsonHelper
import net.minecraft.util.profiling.ProfilerFiller
import org.joml.Vector3f
import java.util.TreeMap

class EntityAnimListener: SimpleJsonListener("geo_entity/animation") {
    override fun apply(
        reads: Map<ResourceLocation, JsonElement>,
        resourceManager: ResourceManager,
        profiler: ProfilerFiller
    ) {
        reads.forEach { id, json ->
            val animations = json.asJsonObject.getAsJsonObject("animations")
            val animationList = arrayListOf<Animation>()
            animations.entrySet().forEach { anim ->
                val animName = anim.key
                val animJson = anim.value.asJsonObject
                val baseLifeTime = GsonHelper.getAsDouble(animJson, "animation_length")
                val bones = GsonHelper.getAsJsonObject(animJson, "bones")
                val boneList = ArrayList<BoneAnim>()
                bones.entrySet().forEach { bone ->
                    val boneName = bone.key
                    val transform = bone.value.asJsonObject
                    val rotations = GsonHelper.getAsJsonObject(transform, "rotation", null)
                    val positions = GsonHelper.getAsJsonObject(transform, "position", null)
                    val rotMap = TreeMap<Double, Vector3f>()
                    val posMap = TreeMap<Double, Vector3f>()
                    rotations?.entrySet()?.forEach { rotation ->
                        val timestamp = rotation.key.toDoubleOrNull()
                        val rotValueArray = if (timestamp != null) GsonHelper.getAsJsonArray(rotation.value.asJsonObject, "vector") else rotation.value.asJsonArray
                        val rot = Vector3f(rotValueArray[0].asFloat, rotValueArray[1].asFloat, rotValueArray[2].asFloat)
                        rotMap.put(timestamp ?: 0.0, rot)
                    }
                    positions?.entrySet()?.forEach { position ->
                        val timestamp = position.key.toDoubleOrNull()
                        val posValueArray = if (timestamp != null) GsonHelper.getAsJsonArray(position.value.asJsonObject, "vector") else position.value.asJsonArray
                        val pos = Vector3f(posValueArray[0].asFloat, posValueArray[1].asFloat, posValueArray[2].asFloat)
                        posMap.put(timestamp ?: 0.0, pos)
                    }
                    boneList.add(BoneAnim(boneName, baseLifeTime, rotMap, posMap))
                }
                animationList.add(Animation(animName, baseLifeTime, boneList))
            }
            AnimationSet.ORIGINS[id] = AnimationSet(animationList)
        }

    }
}