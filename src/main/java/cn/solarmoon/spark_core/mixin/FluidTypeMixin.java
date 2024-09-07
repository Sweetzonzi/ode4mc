package cn.solarmoon.spark_core.mixin;

import cn.solarmoon.spark_core.feature.bucket_fix.BucketFixer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidType.class)
public class FluidTypeMixin {

    @Inject(remap = false, method = "getBucket", at = @At("RETURN"), cancellable = true)
    public void getBucket(FluidStack stack, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack bucket = cir.getReturnValue();
        if (bucket != null) {
            BucketFixer.saveFluidToBucket(stack, bucket);
            cir.setReturnValue(bucket);
        }
    }

}
