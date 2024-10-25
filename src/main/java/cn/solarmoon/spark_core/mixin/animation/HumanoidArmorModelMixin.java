package cn.solarmoon.spark_core.mixin.animation;

import cn.solarmoon.spark_core.api.animation.vanilla.VanillaModelHelper;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(HumanoidArmorModel.class)
public class HumanoidArmorModelMixin<T extends LivingEntity> extends HumanoidModel<T> {

    public HumanoidArmorModelMixin(ModelPart root, Function<ResourceLocation, RenderType> renderType) {
        super(root, renderType);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(ModelPart root, CallbackInfo ci) {
        VanillaModelHelper.setRoot(leftArm, body);
        VanillaModelHelper.setRoot(rightArm, body);
        VanillaModelHelper.setRoot(head, body);
    }

}
