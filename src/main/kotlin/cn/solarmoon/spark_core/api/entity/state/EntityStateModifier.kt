package cn.solarmoon.spark_core.api.entity.state

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.anim.template.EntityStateAnim
import cn.solarmoon.spark_core.api.entity.attack.AttackHelper
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.monster.Husk
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.living.LivingEvent
import net.neoforged.neoforge.event.tick.EntityTickEvent

class EntityStateModifier {

    @SubscribeEvent
    private fun entityTick(event: EntityTickEvent.Post) {
        val entity = event.entity
        val level = entity.level()

        if (!level.isClientSide) {
            if (entity.moveCheck() && !entity.isMoving()) {
                entity.setMoving(true)
            } else if (!entity.moveCheck() && entity.isMoving()) {
                entity.setMoving(false)
            }

            if (entity.moveBackCheck() && !entity.isMovingBack()) {
                entity.setMovingBack(true)
            } else if (!entity.moveBackCheck() && entity.isMovingBack()) {
                entity.setMovingBack(false)
            }

            if (entity.isJumping()) {
                entity.setJumpingState(false)
            }
        }
    }

    @SubscribeEvent
    private fun jump(event: LivingEvent.LivingJumpEvent) {
        val entity = event.entity
        entity.setJumpingState(true)
    }

}