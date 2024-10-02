package cn.solarmoon.spark_core.feature.inlay

import cn.solarmoon.spark_core.api.cap.item.ItemStackHandlerHelper
import cn.solarmoon.spark_core.api.tooltip.CustomTooltip
import cn.solarmoon.spark_core.registry.client.SparkResources
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.inventory.tooltip.TooltipComponent
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.items.ComponentItemHandler


class InlayTooltip(component: Component) : CustomTooltip<InlayTooltip.Component>(component) {

    val inv: ComponentItemHandler = InlayHelper.getInlayHandler(component.stack)
    val inlays = ItemStackHandlerHelper.getStacks(inv)
    val count = inlays.size
    val scale = 0.7f

    override fun getHeight(): Int {
        val row = count / 5 //count 为1时 + custom
        return if (canBeRendered()) (row * 19 * scale).toInt() + 5 else 0
    }

    override fun getWidth(font: Font): Int {
        val column = if (count < 5) count % 5 else 5 //1-4按数量来，大于等于5时就直接设为5
        return if (canBeRendered()) (column * 19 * scale).toInt() else 0
    }

    override fun renderImage(font: Font, x: Int, y: Int, guiGraphics: GuiGraphics) {
        if (canBeRendered()) {
            val poseStack = guiGraphics.pose()
            for (i in 0 until count) {
                poseStack.pushPose()
                val column = i % 5
                val row = i / 5
                poseStack.scale(scale, scale, 1f)
                val pX = Math.round(x / scale) + 18 * column + 1
                val pY = Math.round(y / scale) + 18 * row + 1
                poseStack.translate(0f, 0f, 1000f)
                guiGraphics.blit(SparkResources.INLAY_SLOT_ICON, pX - 1, pY - 1, 0, 0, 18, 18)
                poseStack.translate(0f, 0f, 1000f)
                guiGraphics.renderItem(inlays[i], pX, pY)
                val stackCount: Int = inlays[i].count
                if (stackCount > 1) {
                    val stackCountStr = stackCount.toString()
                    val stringWidth = font.width(stackCountStr)
                    poseStack.translate(0f, 0f, 1000f)
                    guiGraphics.drawString(font, stackCountStr, pX + 18 - stringWidth, pY + 10, 0xFFFFFF)
                }
                poseStack.popPose()
            }
        }
    }

    fun canBeRendered(): Boolean {
        return inlays.isNotEmpty()
    }

    class Component(val stack: ItemStack): TooltipComponent

}