package cn.solarmoon.spark_core.api.phys

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import org.ode4j.math.DVector3
import org.ode4j.ode.DContactBuffer
import org.ode4j.ode.DGeom
import org.ode4j.ode.OdeHelper

class DxBoundingBoxEntity(
    entityType: EntityType<*>,
    level: Level
): DxEntity(entityType, level) {

    val geom = OdeHelper.createBox(DVector3(1.0, 1.0, 1.0))

    init {
        geom.body = body
    }

    override fun getOwner(): Entity? {
        return level().getEntity(entityData[ENTITY_OWNER])
    }

    override fun tick() {
        super.tick()
        getOwner()?.let {
            geom.lengths = DVector3(it.boundingBox.xsize, it.boundingBox.ysize, it.boundingBox.zsize)
            setPos(it.boundingBox.center)
        }
    }

    override fun onCollide(o2: DGeom, buffer: DContactBuffer) {
        (o2.entity.getOwner() as? Entity)?.hurt(damageSources().magic(), 1f)
    }

}