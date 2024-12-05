package cn.solarmoon.spark_core.api.entity.skill

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.phys.copy
import cn.solarmoon.spark_core.api.phys.obb.OrientedBoundingBox
import org.joml.Vector3f

abstract class BoneBoxAnimSkill(
    animatable: IEntityAnimatable<*>,
    animBounds: Set<String>
): AnimSkill(animatable, animBounds) {

    val extendByEntityInteractRange get() = true

    abstract val boxSize: Vector3f

    abstract val boxOffset: Vector3f

    abstract fun getBoxBoundBoneName(anim: MixedAnimation): String

    fun getBoxBoundToBone(anim: MixedAnimation, partialTicks: Float = 0f): OrientedBoundingBox {
        val box = animatable.createCollisionBoxBoundToBone(getBoxBoundBoneName(anim), boxSize.copy(), boxOffset.copy(), partialTicks)
        return if (extendByEntityInteractRange) box.extendByEntityInteractRange(entity) else box
    }

}