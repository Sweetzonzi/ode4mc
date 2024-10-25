package cn.solarmoon.spark_core.mixin.animation;

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable;
import cn.solarmoon.spark_core.api.animation.vanilla.VanillaModelHelper;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin<
        T extends net. minecraft. world. entity. LivingEntity,
        M extends net. minecraft. client. model. HumanoidModel<T>,
        A extends net. minecraft. client. model. HumanoidModel<T>
        > {

    @Shadow @Final private A innerModel;

    @Shadow @Final private A outerModel;

    @Inject(method = "getArmorModelHook", at = @At("HEAD"))
    private void in(T entity, ItemStack itemStack, EquipmentSlot slot, A model, CallbackInfoReturnable<Model> cir) {
        if (entity instanceof IEntityAnimatable<?> animatable) {
            VanillaModelHelper.setPivot(animatable.getAnimData(), "body", innerModel.body);
            VanillaModelHelper.setPivot(animatable.getAnimData(), "body", outerModel.body);
        }
    }

}
