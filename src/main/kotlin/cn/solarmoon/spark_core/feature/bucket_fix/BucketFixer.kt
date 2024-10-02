package cn.solarmoon.spark_core.feature.bucket_fix

import cn.solarmoon.spark_core.registry.common.SparkDataComponents
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.fluids.FluidStack


object BucketFixer {//

    /**
     * 用于mixin，读取桶内保存有的带数据的液体
     * @return 如果桶内没有保存的带数据液体，则不作任何操作，直接返回输入的液体，反之返回保存的液体
     */
    @JvmStatic
    fun readBucketFluid(bucket: ItemStack, stack: FluidStack): FluidStack {
        return bucket.getOrDefault(SparkDataComponents.BUCKET_FLUID, FluidStack.EMPTY).takeIf { !it.isEmpty } ?: stack
    }

    /**
     * 用于mixin，保存带data数据的流体到桶
     */
    @JvmStatic
    fun saveFluidToBucket(stack: FluidStack, bucket: ItemStack) {
        if (!bucket.isEmpty && !stack.components.isEmpty) {
            bucket.update(SparkDataComponents.BUCKET_FLUID, FluidStack.EMPTY) { stack }
        }
    }

}