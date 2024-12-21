package cn.solarmoon.spark_core.api.entity.skill.test

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.phys.DxAnimAttackEntity
import cn.solarmoon.spark_core.api.phys.toDVector3
import org.joml.Vector3d
import org.joml.Vector3f

class SimpleAttackAnimSkill(
    val animName: String,
    val boxSize: Vector3d,
    val boxOffset: Vector3d,
    val shouldAttack: (DxAnimAttackEntity, MixedAnimation) -> Boolean
): Skill {

    override fun activate(ob: Any) {
        if (ob is IEntityAnimatable<*>) {
            ob.animController.stopAndAddAnimation(MixedAnimation(animName))
        }
    }

    override fun tick(ob: Any) {
        if (ob is DxAnimAttackEntity) {
            var flag = false
            ob.getOwner()?.let { animatable ->
                animatable.animData.playData.getMixedAnimation(animName)?.let {
                    if (shouldAttack.invoke(ob, it)) {
                        flag = true
                        ob.geom.lengths = boxSize.toDVector3()
                        ob.geom.offsetPosition = boxOffset.toDVector3()
                        ob.body.enable()
                    }
                }
            }
            if (!flag) ob.body.disable()
        }
    }

}