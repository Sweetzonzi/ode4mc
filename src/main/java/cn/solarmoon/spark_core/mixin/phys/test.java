package cn.solarmoon.spark_core.mixin.phys;

import cn.solarmoon.spark_core.SparkCore;
import org.ode4j.ode.OdeHelper;
import org.ode4j.ode.internal.OdeInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = OdeHelper.class, remap = false)
public class test {

    @Inject(method = "initODE", at = @At("HEAD"))
    private static void init(CallbackInfo ci) {
        SparkCore.LOGGER.debug("你煞笔吧");
    }

}
