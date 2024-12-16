package cn.solarmoon.spark_core.api.phys

import cn.solarmoon.spark_core.api.phys.thread.getPhysWorld
import cn.solarmoon.spark_core.registry.common.SparkVisualEffects
import net.minecraft.world.entity.Entity
import org.ode4j.ode.DBody
import org.ode4j.ode.DGeom
import java.awt.Color

class EntityBoundingBoxBone(
    val entity: Entity,
    name: String? = null
): IBoundingBone {

    override var body: DBody = DxHelper.createNamedBody(entity.getPhysWorld().world, name).apply {
        data().owner = entity
        gravityMode = false
    }

    override var boundingGeoms: MutableList<DGeom> = mutableListOf(
        entity.boundingBox.toDAABB().toDBox(entity.getPhysWorld().space).apply {
            body = this@EntityBoundingBoxBone.body
            data().owner = entity
        }
    )

    override fun tick() {
        body.position = entity.boundingBox.center.toDVector3()
        SparkVisualEffects.OBB.getRenderableBox("${entity.id}:BoundingBox").apply { setColor(Color.RED) }.refresh(geom)
    }

}