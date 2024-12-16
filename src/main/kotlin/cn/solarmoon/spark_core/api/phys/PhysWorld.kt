package cn.solarmoon.spark_core.api.phys

import cn.solarmoon.spark_core.SparkCore
import org.ode4j.ode.DContactBuffer
import org.ode4j.ode.DGeom
import org.ode4j.ode.OdeHelper

class PhysWorld(val stepSize: Long) {

    companion object {
        const val MAX_CONTACT_AMOUNT = 64
    }

    val world = OdeHelper.createWorld()
    val space = OdeHelper.createHashSpace()
    val contactGroup = OdeHelper.createJointGroup()
    val delayActions = mutableListOf<() -> Unit>()

    init {
        world.setGravity(0.0, -9.81, 0.0) //设置重力
        world.contactSurfaceLayer = 0.01 //最大陷入深度，有助于防止抖振(虽然本来似乎也没)
        world.erp = 0.25
        world.cfm = 0.00005
        world.autoDisableFlag = true //设置静止物体自动休眠以节约性能
        world.autoDisableSteps = 5
        world.quickStepNumIterations = 40 //设定迭代次数以提高物理计算精度
        world.quickStepW = 1.3
        world.contactMaxCorrectingVel = 20.0
    }

    fun physTick() {
        world.quickStep(1000.0 / stepSize)
        space.collide(Any(), ::nearCallback)
        contactGroup.empty()
        delayActions.forEach { it.invoke() }
    }

    fun nearCallback(data: Any, o1: DGeom, o2: DGeom) {
        if (o1.data().owner == o2.data().owner) return
        val bufferSize = MAX_CONTACT_AMOUNT
        val contactBuffer = DContactBuffer(bufferSize)
        val contacts = OdeHelper.collide(o1, o2, bufferSize, contactBuffer.geomBuffer)
        if (contacts > 0) {
            o1.data().invoke(o2, contactBuffer)
            o2.data().invoke(o1, contactBuffer)
        }
    }

}