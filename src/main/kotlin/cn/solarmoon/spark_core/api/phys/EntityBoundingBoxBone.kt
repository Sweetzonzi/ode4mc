package cn.solarmoon.spark_core.api.phys

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.phys.thread.getPhysLevel
import kotlinx.coroutines.launch
import net.minecraft.world.entity.Entity
import org.ode4j.ode.DBody
import org.ode4j.ode.DGeom
import org.ode4j.ode.OdeHelper
import thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d.toVector3d

class EntityBoundingBoxBone(
    val entity: Entity,
    name: String? = null
): IBoundingBone {

    override var body: DBody? = null
    override var boundingGeoms: MutableList<DGeom>? = null

    init {
        entity.getPhysLevel()!!.scope.launch {
            body = DxHelper.createNamedBody(entity.getPhysLevel()!!.physWorld.world, name)
            body!!.data().owner = entity
            body!!.gravityMode = false
            val geom = entity.boundingBox.toDAABB().toDBox(entity.getPhysLevel()!!.physWorld.space)
            geom.body = body
            geom.data().owner = entity
            boundingGeoms = mutableListOf<DGeom>(geom)
        }
    }

    override fun physTick() {
        entity.getPhysLevel()!!.launch {
            body?.position = entity.boundingBox.center.toDVector3()
        }
    }

}