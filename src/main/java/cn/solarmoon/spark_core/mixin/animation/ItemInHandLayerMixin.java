package cn.solarmoon.spark_core.mixin.animation;

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private void render(LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext displayContext, HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (livingEntity instanceof IEntityAnimatable<?> animatable) {
            var partialTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
            var boneName = arm == HumanoidArm.LEFT ? "leftItem" : "rightItem";
            var animData = animatable.getAnimData();
            var play = animData.getPlayData();
            var rot = play.getMixedBoneAnimRotation(boneName, partialTicks);
            var pos = play.getMixedBoneAnimPosition(boneName, partialTicks);
            var ma = new Matrix4f().translate(pos.x, -pos.z, pos.y)
                    .rotateZYX(rot.y, -rot.z, rot.x + (float) Math.toRadians(10))
                    .scale(play.getMixedBoneAnimScale(boneName, partialTicks));
            poseStack.translate(0f, -2/16f, 1/16f);
            poseStack.mulPose(ma);
            poseStack.translate(0f, 2/16f, -1/16f);
        }
    }

}
