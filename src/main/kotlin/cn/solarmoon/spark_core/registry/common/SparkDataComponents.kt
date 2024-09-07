package cn.solarmoon.spark_core.registry.common

import cn.solarmoon.spark_core.SparkCore
import net.minecraft.world.item.component.ItemContainerContents
import net.neoforged.neoforge.fluids.FluidStack

object SparkDataComponents {

    @JvmStatic
    fun register() {}

    @JvmStatic
    val INLAY = SparkCore.REGISTER.dataComponent<ItemContainerContents>()
        .id("inlay")
        .build { builder ->
            builder.persistent(ItemContainerContents.CODEC)
                .networkSynchronized(ItemContainerContents.STREAM_CODEC)
                .cacheEncoding()
        }

    @JvmStatic
    val BUCKET_FLUID = SparkCore.REGISTER.dataComponent<FluidStack>()
        .id("bucket_fluid")
        .build {
            it.persistent(FluidStack.OPTIONAL_CODEC)
                .networkSynchronized(FluidStack.OPTIONAL_STREAM_CODEC)
                .cacheEncoding()
        }

}