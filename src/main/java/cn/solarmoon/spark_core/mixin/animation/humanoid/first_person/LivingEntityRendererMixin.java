package cn.solarmoon.spark_core.mixin.animation.humanoid.first_person;

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable;
import cn.solarmoon.spark_core.api.animation.vanilla.PlayerAnimHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Shadow @Final protected List<RenderLayer<T, M>> layers;

    // 这段已知和playerAnimator冲突了，一般重定向冲突的问题怎么解决呢？
    @Redirect(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;layers:Ljava/util/List;", opcode = Opcodes.GETFIELD)
    )
    private List<RenderLayer<T, M>> preventRenderArmorLayerInFirstPersonAnim(LivingEntityRenderer instance, LivingEntity entity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        if (entity instanceof AbstractClientPlayer player && PlayerAnimHelper.shouldRenderArmAnimInFirstPerson(player)) {
            return layers.stream().filter(layer -> layer instanceof PlayerItemInHandLayer<?,?>).toList();
        } else return layers;
    }

}
