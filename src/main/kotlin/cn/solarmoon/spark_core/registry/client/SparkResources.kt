package cn.solarmoon.spark_core.registry.client

import cn.solarmoon.spark_core.SparkCore
import net.minecraft.resources.ResourceLocation

object SparkResources {///

    @JvmStatic
    val JEI_SLOT: ResourceLocation = ResourceLocation.fromNamespaceAndPath(SparkCore.MOD_ID, "textures/gui/jei/slot.png")
    @JvmStatic
    val JEI_CHANCE_SLOT: ResourceLocation = ResourceLocation.fromNamespaceAndPath(SparkCore.MOD_ID, "textures/gui/jei/chance_slot.png")
    @JvmStatic
    val JEI_ARROW: ResourceLocation = ResourceLocation.fromNamespaceAndPath(SparkCore.MOD_ID, "textures/gui/jei/arrow.png")
    @JvmStatic
    val JEI_EMPTY_ARROW: ResourceLocation = ResourceLocation.fromNamespaceAndPath(SparkCore.MOD_ID, "textures/gui/jei/empty_arrow.png")
    @JvmStatic
    val JEI_PLUS: ResourceLocation = ResourceLocation.fromNamespaceAndPath(SparkCore.MOD_ID, "textures/gui/jei/plus.png")
    @JvmStatic
    val JEI_EXP: ResourceLocation = ResourceLocation.fromNamespaceAndPath(SparkCore.MOD_ID, "textures/gui/jei/exp.png")
    @JvmStatic
    val JEI_HAND_POINT: ResourceLocation = ResourceLocation.fromNamespaceAndPath(SparkCore.MOD_ID, "textures/gui/jei/hand.png")

    @JvmStatic
    val INLAY_SLOT_ICON: ResourceLocation = ResourceLocation.fromNamespaceAndPath(SparkCore.MOD_ID, "textures/gui/inlay_slot.png")

}