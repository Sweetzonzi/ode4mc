package cn.solarmoon.spark_core.api.animation.model

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.data.SimpleJsonListener
import cn.solarmoon.spark_core.api.animation.model.part.BonePart
import cn.solarmoon.spark_core.api.animation.model.part.CubePart
import com.google.gson.JsonElement
import com.mojang.serialization.JsonOps
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.GsonHelper
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.world.phys.Vec3
import org.joml.Vector2i

class EntityModelListener: SimpleJsonListener("geo_entity/model") {

    override fun apply(
        reads: Map<ResourceLocation, JsonElement>,
        resourceManager: ResourceManager,
        profiler: ProfilerFiller
    ) {
        reads.forEach { id, json ->
            val target = json.asJsonObject.getAsJsonArray("minecraft:geometry").first().asJsonObject.getAsJsonArray("bones")
            // 单独读取贴图长宽
            val texture = json.asJsonObject.getAsJsonArray("minecraft:geometry").first().asJsonObject.getAsJsonObject("description")
            val coord = Vector2i(GsonHelper.getAsInt(texture, "texture_width"), GsonHelper.getAsInt(texture, "texture_height"))
            val bones = BonePart.LIST_CODEC.decode(JsonOps.INSTANCE, target).orThrow.first.map {
                // 应用长宽到所有方块
                val cubes = it.cubes.map { CubePart(Vec3(-it.originPos.x-it.size.x, it.originPos.y, it.originPos.z), it.size, it.pivot.multiply(-1.0, 1.0, 1.0), it.rotation.multiply(-1.0, -1.0, 1.0), it.inflate, it.uv, it.mirror, coord.x, coord.y) }.toMutableList().asReversed()
                BonePart(it.name, it.parentName, it.pivot.multiply(-1.0, 1.0, 1.0), it.rotation.multiply(-1.0, -1.0, 1.0), ArrayList(cubes))
            }
            CommonModel.ORIGINS[id] = CommonModel(coord.x, coord.y, ArrayList(bones))
        }
    }

}