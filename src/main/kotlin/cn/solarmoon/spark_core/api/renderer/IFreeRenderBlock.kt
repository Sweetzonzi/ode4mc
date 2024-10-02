package cn.solarmoon.spark_core.api.renderer

/**
 * 一般而言，当Block的renderShape返回INVISIBLE时，不仅模型不可见，而且renderBlock方法会换为BlockWithoutLevel的renderer，如果需要将renderBlock方法
 * 还原以在原有模型基础上修改渲染，则可接入此接口
 */
interface IFreeRenderBlock {//
}