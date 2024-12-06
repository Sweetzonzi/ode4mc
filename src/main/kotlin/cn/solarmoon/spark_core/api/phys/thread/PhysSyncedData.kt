package cn.solarmoon.spark_core.api.phys.thread

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.joml.Vector3f
import java.util.concurrent.atomic.AtomicReference

class PhysSyncedData {

    companion object {
        @JvmStatic
        val EMPTY get() = PhysSyncedData()
    }

}
