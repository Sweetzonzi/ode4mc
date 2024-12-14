package cn.solarmoon.spark_core.api.phys

import org.ode4j.ode.DBody
import org.ode4j.ode.DGeom

interface IBoundingBone {

    val body: DBody?
    val boundingGeoms: MutableList<DGeom>?

    fun physTick()

}