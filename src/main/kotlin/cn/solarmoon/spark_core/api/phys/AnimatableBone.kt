package cn.solarmoon.spark_core.api.phys

import cn.solarmoon.spark_core.api.animation.IAnimatable
import cn.solarmoon.spark_core.api.phys.thread.getPhysLevel
import kotlinx.coroutines.launch
import org.joml.Matrix3d
import org.ode4j.ode.DBody
import org.ode4j.ode.DGeom
import org.ode4j.ode.OdeHelper

class AnimatableBone(
    val animatable: IAnimatable<*>,
    private val name: String,
    boundingGeoms: MutableList<DGeom>
): IBoundingBone {

    override var body: DBody? = null
    override var boundingGeoms: MutableList<DGeom>? = null

    init {
        animatable.animatable.getPhysLevel()!!.scope.launch {
            body = DxHelper.createNamedBody(animatable.animatable.getPhysLevel()!!.physWorld.world, name)
            body!!.data().owner = animatable.animatable
            this@AnimatableBone.boundingGeoms = boundingGeoms.apply { forEach {
                it.body = body
                it.data().owner = animatable.animatable
            } }
        }
    }

    override fun physTick() {
        animatable.animatable.getPhysLevel()!!.launch {
            body?.position = animatable.getBonePivot(name).toDVector3()
            body?.rotation = animatable.getBoneMatrix(name).get3x3(Matrix3d()).toDMatrix3()
        }
    }

}