package cn.solarmoon.spark_core.api.entity.skill.test

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.phys.attached_body.EntityAnimatedAttackBody
import cn.solarmoon.spark_core.api.phys.attached_body.getBody
import cn.solarmoon.spark_core.api.phys.toDVector3
import net.neoforged.neoforge.attachment.IAttachmentHolder
import org.joml.Vector3d
import org.joml.Vector3f

class SimpleAttackAnimSkill(
    val animName: String
): Skill<IEntityAnimatable<*>> {

    override fun activate(ob: IEntityAnimatable<*>) {
        ob.animController.stopAndAddAnimation(MixedAnimation(animName))
    }

    override fun tick(ob: IEntityAnimatable<*>) {
        var enable = false
        val aBody = ob.animatable.getBody<EntityAnimatedAttackBody>("attack") ?: return
        ob.animData.playData.getMixedAnimation(animName)?.let {
            if (it.isTickIn(0.05, 0.25)) {
                enable = true
                aBody.enableAttack()
            }
        }
        if (!enable && aBody.isEnabled) {
            aBody.disableAttack()
        }
    }

}