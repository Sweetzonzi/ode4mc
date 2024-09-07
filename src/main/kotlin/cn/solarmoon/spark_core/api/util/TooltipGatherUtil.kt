package cn.solarmoon.spark_core.api.util

import com.mojang.datafixers.util.Either
import net.minecraft.world.inventory.tooltip.TooltipComponent
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.client.event.RenderTooltipEvent


object TooltipGatherUtil {

    /**
     * 把内容收集到第一个空行中
     */
    fun gatherToFirstEmpty(gatherEvent: RenderTooltipEvent.GatherComponents, component: TooltipComponent) {
        val length: Int = gatherEvent.tooltipElements.size
        //顺序寻找第一个空行，没找到就加在末尾
        for (i in 0..length) {
            if (i < length) {
                val either = gatherEvent.tooltipElements[i]
                if (either.left().isPresent && either.left().get().getString().isEmpty()) {
                    gatherEvent.tooltipElements.add(i, Either.right(component))
                    break
                }
            } else gatherEvent.tooltipElements.add(Either.right(component))
        }
    }

}