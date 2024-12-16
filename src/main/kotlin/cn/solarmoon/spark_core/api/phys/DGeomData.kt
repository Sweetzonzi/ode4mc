package cn.solarmoon.spark_core.api.phys

import org.ode4j.ode.DContactBuffer
import org.ode4j.ode.DGeom

class DGeomData: DxData() {

    private var action: ((DGeom, DContactBuffer) -> Unit)? = null
    var passFromCollision = false

    val attackedEntities = mutableSetOf<Int>()

    fun onCollide(action: (DGeom, DContactBuffer) -> Unit) {
        this.action = action
    }

    fun invoke(o2: DGeom, contacts: DContactBuffer) {
        if (o2.data().passFromCollision) return
        action?.invoke(o2, contacts)
    }

}
