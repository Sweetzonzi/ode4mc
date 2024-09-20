package cn.solarmoon.spark_core.api.recipe.processor

import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeInput
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.block.entity.BlockEntity

/**
 * 方块实体专用的配方处理器，简化了绝大多数重复流程，如果不使用默认的预制处理器，则直接接入这个即可
 */
abstract class RecipeProcessor<C: RecipeInput,  R: Recipe<C>>(open val be: BlockEntity) {

    /**
     * 用于快速调用所需配方类型
     */
    abstract fun getRecipeType(): RecipeType<R>

    /**
     * 一般由通用的配方接口实现这个方法<br>
     * 决定了存入的tag名，并可任意调用，默认使用id名
     */
    open fun getName(): String = getRecipeType().toString()

    /**
     * 自动存储配方数据
     */
    abstract fun save(tag: CompoundTag, registries: HolderLookup.Provider)

    /**
     * 自动读取配方数据
     */
    abstract fun load(tag: CompoundTag, registries: HolderLookup.Provider)

    fun saveAll(tag: CompoundTag, registries: HolderLookup.Provider) {
        if (this is IProcessorAssistant) aSave(tag, registries)
        save(tag, registries)
    }

    fun loadAll(tag: CompoundTag, registries: HolderLookup.Provider) {
        if (this is IProcessorAssistant) aLoad(tag, registries)
        load(tag, registries)
    }

}