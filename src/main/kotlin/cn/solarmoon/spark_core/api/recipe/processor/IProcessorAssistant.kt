package cn.solarmoon.spark_core.api.recipe.processor

import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag

/**
 * 辅助配方处理器的接口，需要同样的接口来继承，获得同类型接口后可以任意接入配方处理器类以对配方进行辅助处理
 *
 * 默认可以对配方数据进行辅助处理
 */
interface IProcessorAssistant {

    /**
     * 自动存储配方数据
     */
    fun aSave(tag: CompoundTag, provider: HolderLookup.Provider)

    /**
     * 自动读取配方数据
     */
    fun aLoad(tag: CompoundTag, provider: HolderLookup.Provider)

}