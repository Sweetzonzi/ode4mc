package cn.solarmoon.spark_core.api.network

object CommonNetRegister {

    @JvmStatic
    val HANDLERS = mutableListOf<Pair<ICommonNetHandler, ICommonNetHandler>>()

    @JvmStatic
    fun register(handlerBound: Pair<ICommonNetHandler, ICommonNetHandler>) {
        HANDLERS.add(handlerBound)
    }

}