package cn.solarmoon.spark_core.api.phys

import org.ode4j.ode.DContactBuffer
import org.ode4j.ode.DGeom

class DGeomData: DxData() {

    private var action: ((DGeom, DContactBuffer) -> Unit)? = null

    fun onCollide(action: (DGeom, DContactBuffer) -> Unit) {
        this.action = action
    }

    fun invoke(o2: DGeom, contacts: DContactBuffer) {
        action?.invoke(o2, contacts)
    }

}
