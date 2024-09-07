package cn.solarmoon.spark_core.api.attachment.animation

import cn.solarmoon.spark_core.api.cap.fluid.FluidHandlerHelper
import cn.solarmoon.spark_core.api.phys.SMath
import cn.solarmoon.spark_core.api.renderer.TextureRenderHelper
import cn.solarmoon.spark_core.registry.common.SparkAttachments
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.world.level.block.entity.BlockEntity
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.capability.IFluidHandler
import kotlin.jvm.optionals.getOrElse

object AnimHelper {

    object Fluid {

        /**
         * 在anim所拥有的各个map中，所有和此处基本液体相关的内容所用到的标识名
         */
        const val IDENTIFIER = "Fluid"

        /**
         * 初始化方块实体时调用，可以快速创建默认流体动画组
         */
        @JvmStatic
        fun createFluidAnim(be: BlockEntity) {
            val anim: AnimTicker = be.getData(SparkAttachments.ANIMTICKER)
            val timer = Timer()
            timer.maxTime = 10f
            anim.timers[IDENTIFIER] = timer
        }

        /**
         * 默认当液体储罐改变时开始播放，并在动画计时结束后保存当前液体缓存以用于下一次动画
         * @param fluidStack 设置结束后缓存的液体，如果动画正在进行仍然调用了这个方法（也就是液体连续改变），立刻结束动画并设定当前液体，防止动画停止或断断续续
         */
        @JvmStatic
        fun startFluidAnim(be: BlockEntity, fluidStack: FluidStack) {
            val anim = be.getData(SparkAttachments.ANIMTICKER)
            val timer = anim.timers[IDENTIFIER]!!
            timer.onStop = {
                anim.fixedElements[IDENTIFIER] = FluidStack.OPTIONAL_CODEC.encodeStart(NbtOps.INSTANCE, fluidStack).result().get() as CompoundTag
                be.setChanged()
            }
            // 想找个更平滑的突变算法
            if (timer.isTiming) {
                val p = FluidStack.OPTIONAL_CODEC.parse(NbtOps.INSTANCE, anim.fixedElements[IDENTIFIER]).result().getOrElse { FluidStack.EMPTY }
                p.amount = (p.amount + (fluidStack.amount - p.amount) * timer.getProgress()).toInt()
                anim.fixedElements[IDENTIFIER] = FluidStack.OPTIONAL_CODEC.encodeStart(NbtOps.INSTANCE, p).result().get() as CompoundTag
                return
            }
            timer.start()
        }

        /**
         * 渲染指定液体及其变化后的上升下降过渡动画
         * @param height 液体最大高度
         */
        @JvmStatic
        @OnlyIn(Dist.CLIENT)
        fun renderAnimatedFluid(
            be: BlockEntity,
            side: Direction,
            width: Float,
            height: Float,
            yOffset: Double,
            partialTicks: Float,
            poseStack: PoseStack,
            buffer: MultiBufferSource,
            light: Int
        ) {
            val anim = be.getData(SparkAttachments.ANIMTICKER)
            val timer = anim.timers[IDENTIFIER]!!
            be.level?.getCapability(Capabilities.FluidHandler.BLOCK, be.blockPos, side)?.let { tank ->
                val lastFluid = FluidStack.OPTIONAL_CODEC.parse(NbtOps.INSTANCE, anim.fixedElements[IDENTIFIER]).result().getOrElse { FluidStack.EMPTY }
                val presentFluid = tank.getFluidInTank(0)
                val renderFluid = presentFluid.takeIf { !it.isEmpty } ?: lastFluid
                val color = TextureRenderHelper.getColor(renderFluid)
                val fluidAttributes = IClientFluidTypeExtensions.of(renderFluid.fluid)
                val spriteLocation = fluidAttributes.getStillTexture(renderFluid)

                val presentScale = lastFluid.amount / tank.getTankCapacity(0).toFloat()
                val targetScale = FluidHandlerHelper.getScale(tank)
                val presentH = presentScale * height
                val targetH = targetScale * height
                val hDifference = targetH - presentH
                val realPartialTicks = partialTicks.takeIf { timer.isTiming } ?: 0f
                val progress = timer.getProgress(realPartialTicks)
                val uMax = (width * 16).toInt()
                val vMax = (width * 16).toInt()
                val realH = 0.02f.coerceAtLeast(presentH + SMath.smoothInterpolation(progress, 0f, hDifference, 1.4f)) // 保证最低高度，防止重合到底面时闪烁

                poseStack.pushPose()
                poseStack.translate(0.0, yOffset, 0.0)
                TextureRenderHelper.render(spriteLocation, 0, 0, uMax, vMax, width, realH, color, 1f, 0, poseStack, buffer, light)
                poseStack.popPose()
            }
        }

        /**
         * 按容器液体比例渲染静态液体
         */
        @JvmStatic
        @OnlyIn(Dist.CLIENT)
        fun renderStaticFluid(
            width: Float,
            height: Float,
            yOffset: Float,
            tank: IFluidHandler,
            poseStack: PoseStack,
            buffer: MultiBufferSource,
            light: Int
        ) {
            poseStack.pushPose()
            poseStack.translate(0f, yOffset, 0f)
            val fluidStack = tank.getFluidInTank(0)
            val targetColor = TextureRenderHelper.getColor(fluidStack)
            val fluid = fluidStack.fluid
            val fluidAttributes = IClientFluidTypeExtensions.of(fluid)
            val spriteLocation = fluidAttributes.getStillTexture(fluidStack)
            val h = FluidHandlerHelper.getScale(tank) * height
            TextureRenderHelper.render(
                spriteLocation,
                0, 0, (width * 16).toInt(), (width * 16).toInt(), width, h,
                targetColor, 1f, 0, poseStack, buffer, light
            )
            poseStack.popPose()
        }

    }

}