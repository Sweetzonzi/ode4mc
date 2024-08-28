package cn.solarmoon.solarmoon_core.api.recipe.processor

import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.block.entity.BlockEntity

object RecipeProcessorHelper {

    /**
     * 直接获取对应的配方处理器
     */
    @JvmStatic
    fun <P: RecipeProcessor<*, *>> get(be: BlockEntity, recipeType: RecipeType<*>): P? {
        return try {
            getMap(be)[recipeType] as P?
        } catch (e: ClassCastException) {
            throw RuntimeException("You must first create a list of bindings associated with $recipeType before you get this type of recipe processor")
        }
    }

    /**
     * 获取方块实体所带的所有配方处理器map
     */
    @JvmStatic
    fun getMap(be: BlockEntity): MutableMap<RecipeType<*>, RecipeProcessor<*, *>> {
        return (be as IRecipeProcessorProvider).getRecipeProcessors()
    }

    /**
     * 为方块实体创建多个自定义的配方处理器
     *
     * 可以重复调用，会在已有的基础上添加
     */
    @JvmStatic
    fun createMap(be: BlockEntity, vararg processors: RecipeProcessor<*, *>) {
        val map = getMap(be)
        processors.forEach { processor ->
            map[processor.getRecipeType()] = processor
        }
    }

}