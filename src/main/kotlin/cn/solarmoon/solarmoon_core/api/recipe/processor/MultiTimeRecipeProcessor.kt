package cn.solarmoon.solarmoon_core.api.recipe.processor

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeInput
import net.minecraft.world.level.block.entity.BlockEntity

/**
 * 拥有多个时间的配方处理器，适用于如营火这类的每个格有独立配方时间的配方，自动存储时间数据
 */
abstract class MultiTimeRecipeProcessor<C: RecipeInput,  R: Recipe<C>>(be: BlockEntity) :
    RecipeProcessor<C, R>(be) {

    var times = IntArray(64)
    var recipeTimes = IntArray(64)

    /**
     * 自行实现和调用，尝试匹配配方后工作
     */
    abstract fun tryWork(): Boolean

    /**
     * 自定义配方匹配条件
     * @param recipe 当前所遍历到的配方，直接使用它进行判断即可
     */
    abstract fun isRecipeMatch(recipe: R, index: Int): Boolean

    /**
     * 配方是否正在进行
     */
    open fun isWorking(): Boolean {
        return times.any { it > 0 }
    }

    /**
     * 根据条件寻找配方
     */
    fun findRecipe(index: Int): R? = be.level?.recipeManager?.getAllRecipesFor(getRecipeType())?.firstOrNull { isRecipeMatch(it.value, index) }?.value

    override fun save(tag: CompoundTag) {
        tag.putIntArray(getName() + "Times", times)
        tag.putIntArray(getName() + "RecipeTimes", recipeTimes)
    }

    override fun load(tag: CompoundTag) {
        times = tag.getIntArray(getName() + "Times")
        recipeTimes = tag.getIntArray(getName() + "RecipeTimes")
    }

}