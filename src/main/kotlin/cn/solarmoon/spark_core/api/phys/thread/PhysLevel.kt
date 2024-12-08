package cn.solarmoon.spark_core.api.phys.thread

import cn.solarmoon.spark_core.api.phys.ode.OdeHelper
import cn.solarmoon.spark_core.api.phys.ode.internal.OdeInit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import net.minecraft.world.level.Level

abstract class PhysLevel(
    open val level: Level
) {

    companion object {
        @JvmStatic
        val TICK_STEP = 20L
        @JvmStatic
        val TICKS_PRE_SECOND = (1000.0 / TICK_STEP).toInt()
    }

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    val scope = CoroutineScope(newSingleThreadContext("Phys${level}"))
    val world = OdeHelper.createWorld()
    val entitySpace = OdeHelper.createHashSpace()
    val contactGroup = OdeHelper.createJointGroup()
    protected var lastTickTime = System.nanoTime()

    init {
        OdeInit.dInitODE()
        world.setGravity(0.0, -9.81, 0.0) //设置重力
        world.setContactSurfaceLayer(0.01) //最大陷入深度，有助于防止抖振(虽然本来似乎也没)
        world.setERP(0.25)
        world.setCFM(0.00005)
        world.setAutoDisableFlag(true) //设置静止物体自动休眠以节约性能
        world.setAutoDisableSteps(5)
        world.setQuickStepNumIterations(40) //设定迭代次数以提高物理计算精度
        world.setQuickStepW(1.3)
        world.setContactMaxCorrectingVel(20.0)
        OdeHelper.createPlane(entitySpace, 0.0, 1.0, 0.0, -60.0)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun load() {
        scope.launch {
            while (isActive) {
                val startTime = System.nanoTime()

                physTick()

                val endTime = System.nanoTime()
                lastTickTime = endTime
                val executionTime = (endTime - startTime) / 1_000_000
                val remainingDelay = TICK_STEP - executionTime
                if (remainingDelay > 0) {
                    delay(remainingDelay)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun unLoad() {
        scope.cancel()
    }

    abstract fun physTick()

}