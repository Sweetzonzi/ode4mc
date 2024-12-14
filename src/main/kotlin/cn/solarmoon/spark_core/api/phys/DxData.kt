package cn.solarmoon.spark_core.api.phys

open class DxData {

    companion object {
        private var ID = 0
    }

    var id = ID++
    var owner: Any? = null

}