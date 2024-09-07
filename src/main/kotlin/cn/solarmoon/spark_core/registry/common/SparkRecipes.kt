package cn.solarmoon.spark_core.registry.common

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.feature.inlay.AttributeForgingRecipe
import cn.solarmoon.spark_core.feature.use.UseRecipe

object SparkRecipes {

    @JvmStatic
    fun register() {}

    @JvmStatic
    val ATTRIBUTE_FORGING = SparkCore.REGISTER.recipe<AttributeForgingRecipe>()
        .id("attribute_forging")
        .serializer { AttributeForgingRecipe.Serializer() }
        .build()

    @JvmStatic
    val USE = SparkCore.REGISTER.recipe<UseRecipe>()
        .id("use")
        .serializer { UseRecipe.Serializer() }
        .build()

}