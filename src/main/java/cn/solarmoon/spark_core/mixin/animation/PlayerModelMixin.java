package cn.solarmoon.spark_core.mixin.animation;

import cn.solarmoon.spark_core.api.animation.vanilla.VanillaModelHelper;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(PlayerModel.class)
public class PlayerModelMixin<T extends LivingEntity> extends HumanoidModel<T> {

    @Shadow @Final public ModelPart leftSleeve;

    @Shadow @Final public ModelPart rightSleeve;

    public PlayerModelMixin(ModelPart root, Function<ResourceLocation, RenderType> renderType) {
        super(root, renderType);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("HEAD"))
    private void setup(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        VanillaModelHelper.setRoot(leftSleeve, leftArm);
        VanillaModelHelper.setRoot(rightSleeve, rightArm);
    }

}
