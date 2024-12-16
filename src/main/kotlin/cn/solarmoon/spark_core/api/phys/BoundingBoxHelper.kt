package cn.solarmoon.spark_core.api.phys

import cn.solarmoon.spark_core.api.animation.IAnimatable
import cn.solarmoon.spark_core.api.animation.model.CommonModel
import cn.solarmoon.spark_core.api.phys.thread.getPhysWorld
import cn.solarmoon.spark_core.registry.common.SparkAttachments
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.attachment.IAttachmentHolder
import org.joml.Matrix4f
import org.ode4j.math.DVector3
import org.ode4j.ode.OdeHelper

fun IAttachmentHolder.getBoundingBones() = getData(SparkAttachments.BOUNDING_BONES)

fun IAttachmentHolder.getBoundingBone(name: String): IBoundingBone {
    return getBoundingBones()[name]!!
}

fun IAttachmentHolder.putBoundingBone(bone: IBoundingBone) {
    getBoundingBones().put(bone.body.data().name, bone)
}

fun IAnimatable<*>.putAllAnimatableBones() {
    animData.model.bones.forEach { bone ->
        animatable.putBoundingBone(
            AnimatableBone(this, bone.name, buildList {
                bone.cubes.forEach {
                    add(OdeHelper.createBox(level.getPhysWorld().space, it.size.toDVector3()))
                }
            }.toMutableList()) {
                it.forEachIndexed { index, geom ->
                    val cube = bone.cubes[index]
                    geom.offsetPosition = cube.getTransformedCenter(Matrix4f()).sub(bone.pivot.toVector3f()).toDVector3()
                }
            }
        )
    }
}