package cn.solarmoon.spark_core.api.animation.anim.play

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.anim.template.EntityStateAnim
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.EntityTickEvent

class AnimTicker {

    @SubscribeEvent
    private fun entityTick(event: EntityTickEvent.Pre) {
        val entity = event.entity
        val level = entity.level()
        if (entity is IEntityAnimatable<*>) {
            // 基本tick
            entity.animController.animTick()

            // 状态动画
            if (EntityStateAnim.shouldPlayStateAnim(entity)) {
                for (state in entity.statusAnims.sortedBy { it.priority }) {
                    // 和跳跃进行混合
                    if (state.tryPlay(entity, stopFilter = { state.animName == EntityStateAnim.JUMP.animName || it.name != EntityStateAnim.JUMP.animName })) break
                }
            }
        }
    }

}