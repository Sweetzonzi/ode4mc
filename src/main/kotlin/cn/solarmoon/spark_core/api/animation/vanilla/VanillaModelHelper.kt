package cn.solarmoon.spark_core.api.animation.vanilla

import cn.solarmoon.spark_core.api.animation.anim.play.AnimData
import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.anim.part.BoneAnim
import cn.solarmoon.spark_core.api.animation.model.CommonModel
import net.minecraft.client.Minecraft
import net.minecraft.client.model.geom.ModelPart
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import org.joml.Vector3f
import kotlin.math.PI

@OnlyIn(Dist.CLIENT)
object VanillaModelHelper {

    @JvmStatic
    fun setPivot(animData: AnimData, boneName: String, model: ModelPart) {
        (model as IPivotModelPart).pivot.set(animData.model.getBone(boneName).pivot.toVector3f())
    }

    @JvmStatic
    fun setPivot(commonModel: CommonModel, boneName: String, model: ModelPart) {
        (model as IPivotModelPart).pivot.set(commonModel.getBone(boneName).pivot.toVector3f())
    }

    @JvmStatic
    fun setRoot(child: ModelPart, root: ModelPart) {
        (child as IPivotModelPart).root = root
    }

    @JvmStatic
    fun applyStartTransform(animData: AnimData, boneName: String, part: ModelPart, partialTicks: Float) {
        val pos = animData.playData.getMixedBoneAnimPosition(boneName, partialTicks)
        val rot = animData.playData.getMixedBoneAnimRotation(boneName, partialTicks)
        val scale = animData.playData.getMixedBoneAnimScale(boneName, partialTicks)
        part.setRotation(-rot.x, -rot.y, rot.z)
        part.offsetPos(pos.mul(16f).apply { x = -x; y = -y })
        part.xScale = scale.x; part.yScale = scale.y; part.zScale = scale.z
    }

}