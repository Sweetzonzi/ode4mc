package cn.solarmoon.spark_core.feature.inlay

import cn.solarmoon.spark_core.registry.common.SparkDataComponents
import com.google.common.collect.ImmutableList
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.neoforged.neoforge.items.ComponentItemHandler

object InlayHelper {//

    @JvmStatic
    fun addAttributeToItem(stack: ItemStack, recipe: AttributeForgingRecipe) {
        val ci = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)
        val content = ci.modifiers.toMutableList()
        val modifiers = stack.item.getDefaultAttributeModifiers(stack).modifiers.toMutableList()
        if (content.isEmpty()) content.addAll(modifiers) // 如果data的修改符为空，则加入默认的修改符，不为空则不用加入因为不为空时默认覆盖默认的修改符

        val builder = ImmutableList.builderWithExpectedSize<ItemAttributeModifiers.Entry>(content.size + 1)

        // 如果有和原有属性类型一致的，则在原有基础上加数值，否则直接加一条新的属性
        var findSame = false
        for (entry in content) {
            if (entry.matches(recipe.attributeData.holder, recipe.attributeData.attributeModifier.id)) {
                builder.add(ItemAttributeModifiers.Entry(entry.attribute,
                    AttributeModifier(
                        entry.modifier.id,
                        entry.modifier.amount + recipe.attributeData.attributeModifier.amount,
                        entry.modifier.operation),
                    entry.slot))
                findSame = true
            } else builder.add(entry)
        }

        if (!findSame) builder.add(ItemAttributeModifiers.Entry(recipe.attributeData.holder, recipe.attributeData.attributeModifier, recipe.slot))
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers(builder.build(), ci.showInTooltip))
    }

    /**
     * 获取可以处理插槽内容物的处理类
     */
    @JvmStatic
    fun getInlayHandler(stack: ItemStack) = ComponentItemHandler(stack, SparkDataComponents.INLAY.get(), 16)

    /**
     * @param inlayMaster 插槽母体
     * @return 获取该物品插槽内所有相同类型物品的数量
     */
    @JvmStatic
    fun getSameItemCount(itemType: Item, inlayMaster: ItemStack): Int {
        val handler = getInlayHandler(inlayMaster)
        return (0 until handler.slots).fold(0) { sum, i ->
            val stack = handler.getStackInSlot(i)
            if (stack.`is`(itemType)) sum + stack.count else sum
        }
    }

}