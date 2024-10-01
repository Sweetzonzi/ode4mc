package cn.solarmoon.spark_core.mixin;

import cn.solarmoon.spark_core.api.renderer.IFreeRenderBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockRenderDispatcher.class)
public abstract class BlockRenderDispatcherMixin {

    @Shadow public abstract BakedModel getBlockModel(BlockState state);

    @Shadow @Final private BlockColors blockColors;

    @Shadow @Final private ModelBlockRenderer modelRenderer;

    @Inject(remap = false, method = "renderSingleBlock(Lnet/minecraft/world/level/block/state/BlockState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/neoforged/neoforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V", at = @At("RETURN"))
    public void renderSingleBlock(BlockState state, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, ModelData modelData, RenderType renderType, CallbackInfo ci) {
        Block block = state.getBlock(); //
        if (state.getRenderShape() == RenderShape.INVISIBLE && block instanceof IFreeRenderBlock) {
            BakedModel bakedmodel = this.getBlockModel(state);
            int i = this.blockColors.getColor(state, null, null, 0);
            float f = (float) (i >> 16 & 255) / 255.0F;
            float f1 = (float) (i >> 8 & 255) / 255.0F;
            float f2 = (float) (i & 255) / 255.0F;
            for (net.minecraft.client.renderer.RenderType rt : bakedmodel.getRenderTypes(state, RandomSource.create(42), modelData))
                this.modelRenderer.renderModel(poseStack.last(), buffer.getBuffer(renderType != null ? renderType : RenderTypeHelper.getEntityRenderType(rt, false)), state, bakedmodel, f, f1, f2, light, overlay, modelData, rt);
        }
    }

}
