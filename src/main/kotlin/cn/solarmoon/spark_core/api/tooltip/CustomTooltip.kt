package cn.solarmoon.spark_core.api.tooltip

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.world.inventory.tooltip.TooltipComponent

/**
 * 可深度自定义的tooltip类，包括样式、图片、特殊渲染等
 * @param component 展示tooltip的对象内容类（如item等，需要接入[TooltipComponent])
 */
abstract class CustomTooltip<T: TooltipComponent>(val component: T): ClientTooltipComponent//