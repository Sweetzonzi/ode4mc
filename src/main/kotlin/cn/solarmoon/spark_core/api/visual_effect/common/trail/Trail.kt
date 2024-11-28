package cn.solarmoon.spark_core.api.visual_effect.common.trail

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.phys.obb.OrientedBoundingBox
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import org.joml.Vector3f
import java.awt.Color

class Trail(
    val box: OrientedBoundingBox,
    val axis: Direction.Axis,
    val color: Color = Color.WHITE
) {

    var textureLocation = ResourceLocation.withDefaultNamespace("textures/block/dirt.png")
    val start: Vector3f get() = box.getAxisFaceCenters(axis).first
    val end: Vector3f get() = box.getAxisFaceCenters(axis).second

    var tick = 0
    var maxTick = 5

    val isFinished get() = tick > maxTick
    fun getProgress(partialTicks: Float = 0f) = ((tick + partialTicks) / maxTick).coerceIn(0f, 1f)

    fun tick() {
        if (isFinished) return
        tick += 1
    }

    fun setTexture(itemStack: ItemStack) {
        val id = BuiltInRegistries.ITEM.getKey(itemStack.item)
        setTexture(ResourceLocation.fromNamespaceAndPath(id.namespace, "textures/item/${id.path}.png"))
    }

    fun setTexture(location: ResourceLocation) {
        textureLocation = location
    }

}