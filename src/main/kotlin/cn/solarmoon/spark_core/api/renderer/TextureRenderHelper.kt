package cn.solarmoon.spark_core.api.renderer

import com.google.common.base.Functions
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.collect.Maps
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.Material
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.FastColor
import net.minecraft.world.inventory.InventoryMenu
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions
import net.neoforged.neoforge.fluids.FluidStack
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

object TextureRenderHelper {

    @JvmStatic
    fun render(
        texture: ResourceLocation?,
        minU: Int,
        minV: Int,
        maxU: Int,
        maxV: Int,
        width: Float,
        height: Float,
        color: Int,
        alpha: Float,
        luminosity: Int,
        poseStack: PoseStack,
        bufferIn: MultiBufferSource,
        light: Int
    ) {
        var light = light
        if (texture == null) return
        poseStack.pushPose()
        if (luminosity != 0) light = light and 15728640 or (luminosity shl 4)
        val builder = getBlockMaterial(texture).buffer(bufferIn) { location: ResourceLocation? -> location?.let {
            RenderType.entityTranslucentCull(it)
        } }
        poseStack.translate(0.5, 0.0, 0.5)
        addCube(
            builder, poseStack,
            minU / 16f, minV / 16f,
            maxU / 16f, maxV / 16f,
            width, height,
            light, color, alpha, true, true, true
        )
        poseStack.popPose()
    }

    @JvmStatic
    fun renderFluid(
        color: Int, alpha: Float, luminosity: Int, minU: Int, minV: Int, maxU: Int, maxV: Int,
        texture: ResourceLocation?, poseStack: PoseStack, bufferIn: MultiBufferSource, light: Int
    ) {
        var light = light
        if (texture == null) return
        poseStack.pushPose()
        if (luminosity != 0) light = light and 15728640 or (luminosity shl 4)
        val builder = getBlockMaterial(texture).buffer(bufferIn) { location: ResourceLocation? -> location?.let {
            RenderType.entityTranslucentCull(it)
        } }
        poseStack.translate(0.5, 0.0, 0.5)
        addCube(
            builder, poseStack,
            minU / 16f, minV / 16f,
            maxU / 16f, maxV / 16f,
            1f, 1f,
            light, color, alpha, true, true, true
        )
        poseStack.popPose()
    }

    @JvmStatic
    val CACHED_MATERIALS: Cache<ResourceLocation, Material> = CacheBuilder.newBuilder()
        .expireAfterAccess(2, TimeUnit.MINUTES)
        .build()

    fun getBlockMaterial(bockTexture: ResourceLocation): Material {
        try {
            return CACHED_MATERIALS[bockTexture, {
                Material(
                    TextureAtlas.LOCATION_BLOCKS,
                    bockTexture
                )
            }]
        } catch (e: ExecutionException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 获取液体颜色
     */
    @JvmStatic
    fun getColor(fluidStack: FluidStack): Int {
        val fluid = fluidStack.fluid
        val handler = IClientFluidTypeExtensions.of(fluid)
        return handler.getTintColor(fluidStack)
    }

    /**
     * 获取液体颜色ARGB
     */
    @JvmStatic
    fun getColorARGB(fluidStack: FluidStack): FloatArray {
        val fluidColor = getColor(fluidStack)
        val colorArray = FloatArray(4)
        colorArray[0] = (fluidColor shr 16 and 0xFF) / 255.0f //红
        colorArray[1] = (fluidColor shr 8 and 0xFF) / 255.0f //绿
        colorArray[2] = (fluidColor and 0xFF) / 255.0f //蓝
        colorArray[3] = ((fluidColor shr 24) and 0xFF) / 255f //透明度
        return colorArray
    }

    enum class FluidFlow {
        STILL, FLOWING
    }

    @JvmStatic
    fun getFluidTexture(fluidStack: FluidStack, type: FluidFlow): TextureAtlasSprite? {
        if (fluidStack.isEmpty) return null
        val fluid = fluidStack.fluid
        val spriteLocation: ResourceLocation
        val fluidAttributes = IClientFluidTypeExtensions.of(fluid)
        spriteLocation = if (type == FluidFlow.STILL) {
            fluidAttributes.getStillTexture(fluidStack)
        } else {
            fluidAttributes.getFlowingTexture(fluidStack)
        }
        return getSprite(spriteLocation)
    }

    @JvmStatic
    fun getSprite(spriteLocation: ResourceLocation?): TextureAtlasSprite {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(spriteLocation)
    }

    @JvmStatic
    fun addCube(
        builder: VertexConsumer, poseStack: PoseStack,
        width: Float, height: Float, light: Int, color: Int
    ) {
        addCube(builder, poseStack, 0f, 0f, width, height, light, color)
    }

    @JvmStatic
    fun addCube(
        builder: VertexConsumer, poseStack: PoseStack,
        uOff: Float, vOff: Float,
        width: Float, height: Float, light: Int, color: Int
    ) {
        addCube(
            builder, poseStack, uOff, vOff,
            width, height, light, color, 1f, true, true, true
        )
    }

    @JvmStatic
    fun addCube(
        builder: VertexConsumer, poseStack: PoseStack,
        uOff: Float, vOff: Float,
        w: Float, h: Float, combinedLightIn: Int,
        color: Int, alpha: Float,
        up: Boolean, down: Boolean, wrap: Boolean
    ) {
        addCube(
            builder,
            poseStack,
            uOff,
            1 - (vOff + h),
            uOff + w,
            1 - vOff,
            w,
            h,
            combinedLightIn,
            color,
            alpha,
            up,
            down,
            wrap
        )
    }

    @JvmStatic
    val XN90: Quaternionf = Axis.XP.rotationDegrees(-90f)

    @JvmStatic
    val DIR2ROT: Map<Direction, Quaternionf> = Maps.newEnumMap(
        Arrays.stream(Direction.entries.toTypedArray())
            .collect(
                Collectors.toMap(Functions.identity()) { d ->
                    d.opposite.rotation.mul(XN90)
                }
            )
    )

    @JvmStatic
    fun addCube(
        builder: VertexConsumer, poseStack: PoseStack,
        minU: Float, minV: Float,
        maxU: Float, maxV: Float,
        w: Float, h: Float,
        combinedLightIn: Int,
        color: Int,
        alpha: Float,
        up: Boolean, down: Boolean, wrap: Boolean
    ) {
        val lu = combinedLightIn and '\uffff'.code
        val lv = combinedLightIn shr 16 and '\uffff'.code
        val minV2 = maxV - w

        val r = FastColor.ARGB32.red(color)
        val g = FastColor.ARGB32.green(color)
        val b = FastColor.ARGB32.blue(color)
        val a = (255 * alpha).toInt()

        val hw = w / 2f
        val hh = h / 2f

        var inc = 0f

        poseStack.pushPose()
        poseStack.translate(0f, hh, 0f)
        for (d in Direction.entries) {
            var v0 = minV
            var t = hw
            var y0 = -hh
            var y1 = hh
            val i = inc
            if (d.axis === Direction.Axis.Y) {
                if ((!up && d == Direction.UP) || !down) continue
                t = hh
                y0 = -hw
                y1 = hw
                v0 = minV2
            } else if (!wrap) {
                inc += w
            }
            poseStack.pushPose()
            DIR2ROT[d]?.let { poseStack.mulPose(it) }
            poseStack.translate(0f, 0f, -t)
            addQuad(builder, poseStack, -hw, y0, hw, y1, minU + i, v0, maxU + i, maxV, r, g, b, a, lu, lv)
            poseStack.popPose()
        }
        poseStack.popPose()
    }

    @JvmStatic
    fun addQuad(
        builder: VertexConsumer, poseStack: PoseStack,
        x0: Float, y0: Float,
        x1: Float, y1: Float,
        u0: Float, v0: Float,
        u1: Float, v1: Float,
        r: Int, g: Int, b: Int, a: Int,
        lu: Int, lv: Int
    ) {
        val last = poseStack.last()
        val vector3f = last.normal().transform(Vector3f(0f, 0f, -1f))
        val nx = vector3f.x
        val ny = vector3f.y
        val nz = vector3f.z
        vertF(builder, poseStack, x0, y1, 0f, u0, v0, r, g, b, a, lu, lv, nx, ny, nz)
        vertF(builder, poseStack, x1, y1, 0f, u1, v0, r, g, b, a, lu, lv, nx, ny, nz)
        vertF(builder, poseStack, x1, y0, 0f, u1, v1, r, g, b, a, lu, lv, nx, ny, nz)
        vertF(builder, poseStack, x0, y0, 0f, u0, v1, r, g, b, a, lu, lv, nx, ny, nz)
    }

    @JvmStatic
    private fun vertF(
        builder: VertexConsumer, poseStack: PoseStack, x: Float, y: Float, z: Float,
        u: Float, v: Float,
        r: Int, g: Int, b: Int, a: Int,
        lu: Int, lv: Int, nx: Float, ny: Float, nz: Float
    ) {
        builder.addVertex(poseStack.last().pose(), x, y, z)
        builder.setColor(r, g, b, a)
        builder.setUv(u, v)
        builder.setOverlay(10)
        builder.setUv2(lu, lv)
        builder.setNormal(nx, ny, nz)
    }

}