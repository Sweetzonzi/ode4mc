package cn.solarmoon.spark_core.api.visual_effect

interface IVisualEffect {

    /**
     * 设定添加渲染的逻辑，比如残影是添加到生物->残影的map中，每个视效都不尽相同，故都单独设置
     */
    fun addToRenderer()

}