package cn.solarmoon.spark_core.api.phys.thread

import java.util.concurrent.ConcurrentLinkedQueue

interface IActionConsumer {

    fun getActions(): ConcurrentLinkedQueue<() -> Unit>

}