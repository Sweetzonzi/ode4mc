package cn.solarmoon.solarmoon_core.api.tooltip

import cn.solarmoon.solarmoon_core.SolarMoonCore
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.util.StringUtil
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffectUtil
import net.minecraft.world.item.alchemy.PotionContents

/**
 * 工具提示统一方法，规范了工具提示的格式，并添加工具提示的实用方法
 */
class TooltipOperator(private val tooltips: MutableList<Component>) {

    /**
     * 添加默认样式shift展开的信息
     * @param adder shift展开后调用的操作
     */
    fun addShiftShowTooltip(adder: (TooltipOperator) -> Unit) {
        if (Screen.hasShiftDown()) {
            tooltips.add(SolarMoonCore.TRANSLATOR.set("tooltip", "shift_on"))
            adder.invoke(this)
        } else {
            tooltips.add(SolarMoonCore.TRANSLATOR.set("tooltip", "shift_off"))
        }
    }

    /**
     * @return 添加仿药水效果的工具提示（如：着火（00:10））
     */
    fun addPotionLikeTooltip(name: Component, tickDuration: Int) {
        tooltips.add(Component.translatable(
            "potion.withDuration",
            name.copy(),
            StringUtil.formatTickDuration(tickDuration, 20f)
        ).withStyle(ChatFormatting.BLUE))
    }

    /**
     * 同原版药水提示方法[PotionContents.addPotionTooltip]添加药水效果提示，但是不会写出末尾的属性（如 生效后：抬升高度+1，不会显示这个）
     */
    fun addPotionTooltipWithoutAttribute(effects: List<MobEffectInstance>) {
        if (effects.isNotEmpty()) {
            for (mobeffectinstance in effects) {
                var mutablecomponent = Component.translatable(mobeffectinstance.descriptionId)
                val mobeffect = mobeffectinstance.effect.value()

                if (mobeffectinstance.amplifier > 0) {
                    mutablecomponent = Component.translatable(
                        "potion.withAmplifier",
                        mutablecomponent,
                        Component.translatable("potion.potency." + mobeffectinstance.amplifier)
                    )
                }

                if (!mobeffectinstance.endsWithin(20)) {
                    mutablecomponent = Component.translatable(
                        "potion.withDuration",
                        mutablecomponent,
                        MobEffectUtil.formatDuration(mobeffectinstance, 1f, 20f)
                    )
                }

                tooltips.add(mutablecomponent.withStyle(mobeffect.category.tooltipFormatting))
            }
        }
    }

}
