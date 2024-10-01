package cn.solarmoon.spark_core.mixin;

import cn.solarmoon.spark_core.api.phys.collision.FreeCollisionBoxRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {

    private FreeCollisionBoxRenderer freeBox;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Minecraft minecraft, CallbackInfo ci) {
        freeBox = new FreeCollisionBoxRenderer(minecraft);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, double camX, double camY, double camZ, CallbackInfo ci) {
        freeBox.render(poseStack, bufferSource, camX, camY, camZ);//
    }

}
