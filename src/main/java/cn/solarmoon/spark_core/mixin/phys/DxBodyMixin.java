package cn.solarmoon.spark_core.mixin.phys;

import cn.solarmoon.spark_core.SparkCore;
import cn.solarmoon.spark_core.api.phys.DBodyData;
import cn.solarmoon.spark_core.api.phys.DxHelper;
import org.ode4j.ode.internal.DxBody;
import org.ode4j.ode.internal.DxGeom;
import org.ode4j.ode.internal.DxWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DxBody.class)
public abstract class DxBodyMixin {

    @Shadow public abstract void setData(Object data);

    private final DxBody body = (DxBody) (Object) this;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(DxWorld w, CallbackInfo ci) {
        var data = new DBodyData();
        DxHelper.getALL_BODYS().put(data.getId(), body);
        setData(data);
        SparkCore.LOGGER.debug("你煞笔吧");
    }

}
