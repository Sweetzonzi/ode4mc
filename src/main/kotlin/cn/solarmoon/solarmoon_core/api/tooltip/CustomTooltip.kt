package cn.solarmoon.solarmoon_core.api.tooltip

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.world.inventory.tooltip.TooltipComponent

/**
 * 可深度自定义的tooltip类，包括样式、图片、特殊渲染等
 */
abstract class CustomTooltip<T: TooltipComponent>: ClientTooltipComponent {

    abstract val component: T

}