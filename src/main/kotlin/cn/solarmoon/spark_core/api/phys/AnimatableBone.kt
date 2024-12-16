package cn.solarmoon.spark_core.api.phys

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.IAnimatable
import cn.solarmoon.spark_core.api.phys.thread.getPhysWorld
import cn.solarmoon.spark_core.registry.common.SparkVisualEffects
import kotlinx.coroutines.launch
import org.joml.Matrix3d
import org.joml.Quaterniond
import org.ode4j.ode.DBody
import org.ode4j.ode.DGeom
import org.ode4j.ode.OdeHelper
import java.awt.Color

class AnimatableBone(
    val animatable: IAnimatable<*>,
    private val name: String,
    boundingGeoms: MutableList<DGeom>,
    modifierAfterMaintainBody: (MutableList<DGeom>) -> Unit = {}
): IBoundingBone {

    override var body: DBody = DxHelper.createNamedBody(animatable.level.getPhysWorld().world, name).apply {
        data().owner = animatable.animatable
        gravityMode = false
    }
    override var boundingGeoms: MutableList<DGeom> = boundingGeoms.apply { forEach {
        it.body = body
        it.data().owner = animatable.animatable
        modifierAfterMaintainBody.invoke(this)
    } }

    override fun tick() {
        body.position = animatable.getBonePivot(name).toDVector3()
        body.quaternion = animatable.getBoneMatrix(name).getUnnormalizedRotation(Quaterniond()).toDQuaternion()
        boundingGeoms.forEachIndexed { index, it ->
            SparkVisualEffects.OBB.getRenderableBox("$index$name").apply { setColor(Color.RED) }.refresh(it)
        }
    }

}