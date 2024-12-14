package cn.solarmoon.spark_core.mixin.phys;

import cn.solarmoon.spark_core.api.phys.DGeomData;
import cn.solarmoon.spark_core.api.phys.DxHelper;
import org.ode4j.ode.internal.DxGeom;
import org.ode4j.ode.internal.DxSpace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DxGeom.class)
public abstract class DxGeomMixin {

    @Shadow public abstract void setData(Object data);

    private final DxGeom geom = (DxGeom) (Object) this;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(DxSpace space, boolean isPlaceable, CallbackInfo ci) {
        var data = new DGeomData();
        setData(data);
        DxHelper.getALL_GEOMS().put(data.getId(), geom);
    }

}
