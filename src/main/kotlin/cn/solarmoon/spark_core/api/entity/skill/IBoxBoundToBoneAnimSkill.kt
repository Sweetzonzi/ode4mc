package cn.solarmoon.spark_core.api.entity.skill

import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.phys.copy
import cn.solarmoon.spark_core.api.phys.obb.OrientedBoundingBox
import org.joml.Vector3f

/**
 * 为了简化box的获取而设立的接口，将会创建一个绑定在指定骨骼上的box
 *
 * 其中，为了能更好的兼容自定义动画，box的大小和偏移皆是固定的，如果想要修改还是请单独修改[AnimSkill.getBox]方法
 */
interface IBoxBoundToBoneAnimSkill {

    val self get() = this as AnimSkill

    val extendByEntityInteractRange get() = true

    val boxSize: Vector3f

    val boxOffset: Vector3f

    fun getBoundBoneName(anim: MixedAnimation): String

    fun getBoundBox(anim: MixedAnimation, partialTicks: Float = 0f): OrientedBoundingBox {
        val box = self.animatable.createCollisionBoxBoundToBone(getBoundBoneName(anim), boxSize.copy(), boxOffset.copy(), partialTicks)
        return if (extendByEntityInteractRange) box.extendByEntityInteractRange(self.entity) else box
    }


}