package cn.solarmoon.spark_core.mixin;

import cn.solarmoon.spark_core.feature.bucket_fix.BucketFixer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidBucketWrapper.class)
public class FluidBucketWrapperMixin {

    @Shadow
    protected ItemStack container;

    @Inject(remap = false, method = "getFluid", at = @At("RETURN"), cancellable = true)
    public void getFluid(CallbackInfoReturnable<FluidStack> cir) {
        FluidStack fluidStack = cir.getReturnValue();
        if (fluidStack != null) {
            cir.setReturnValue(BucketFixer.readBucketFluid(container, fluidStack));
        }
    }

}
