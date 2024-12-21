package cn.solarmoon.spark_core.api.phys

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.registry.common.SparkSkills
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import org.joml.Quaterniond
import org.ode4j.math.DVector3
import org.ode4j.ode.DContactBuffer
import org.ode4j.ode.DGeom
import org.ode4j.ode.OdeHelper
import thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d.toVec3

class DxAnimAttackEntity(
    entityType: EntityType<*>,
    level: Level
): DxEntity(entityType, level) {

    val geom = OdeHelper.createBox(DVector3(1.0, 1.0, 1.0))

    init {
        geom.body = body
        body.disable()
    }

    override fun getOwner(): IEntityAnimatable<*>? {
        return level().getEntity(entityData[ENTITY_OWNER]) as? IEntityAnimatable<*>
    }

    override fun tick() {
        super.tick()
        getOwner()?.let {
            if (it.animData.model.hasBone(bodyName)) {
                setPos(it.getBonePivot(bodyName).toVec3())
                body.quaternion = it.getBoneMatrix(bodyName).getUnnormalizedRotation(Quaterniond()).toDQuaternion()
            }
        }
        SparkSkills.PLAYER_SWORD_COMBO_0.value().tick(this)
    }

    override fun onCollide(o2: DGeom, buffer: DContactBuffer) {
        val owner = getOwner() ?: return
        val o2Owner = o2.entity.getOwner() as? Entity ?: return
        if (owner is Player) owner.attack(o2Owner)
        else if (owner is LivingEntity) owner.doHurtTarget(o2Owner)
    }

}