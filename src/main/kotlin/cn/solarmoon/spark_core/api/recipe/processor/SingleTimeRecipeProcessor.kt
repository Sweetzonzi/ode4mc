package cn.solarmoon.spark_core.api.recipe.processor

import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeInput
import net.minecraft.world.level.block.entity.BlockEntity

/**
 * 拥有单个时间的配方处理器，自动存储时间数据
 */
abstract class SingleTimeRecipeProcessor<C: RecipeInput,  R: Recipe<C>>(be: BlockEntity) :
    RecipeProcessor<C, R>(be) {

    var time = 0
    var recipeTime = 0

    /**
     * 自行实现和调用，尝试匹配配方后工作
     */
    abstract fun tryWork(): Boolean

    /**
     * 自定义配方匹配条件
     * @param recipe 当前所遍历到的配方，直接使用它进行判断即可
     */
    abstract fun isRecipeMatch(recipe: R): Boolean

    /**
     * 配方是否正在进行
     */
    open fun isWorking(): Boolean {
        return time > 0
    }

    /**
     * 根据条件寻找配方
     */
    fun findRecipe(): RecipeHolder<R>? = be.level?.recipeManager?.getAllRecipesFor(getRecipeType())?.firstOrNull { isRecipeMatch(it.value) }

    override fun save(tag: CompoundTag, registries: HolderLookup.Provider) {
        tag.putInt(getName() + "Time", time)
        tag.putInt(getName() + "RecipeTime", recipeTime)
    }

    override fun load(tag: CompoundTag, registries: HolderLookup.Provider) {
        time = tag.getInt(getName() + "Time")
        recipeTime = tag.getInt(getName() + "RecipeTime")
    }

}