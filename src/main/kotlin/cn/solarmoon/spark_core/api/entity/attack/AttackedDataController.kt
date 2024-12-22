package cn.solarmoon.spark_core.api.entity.attack

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.phys.attached_body.AnimatedCubeBody
import cn.solarmoon.spark_core.api.phys.attached_body.EntityAnimatedAttackBody
import cn.solarmoon.spark_core.api.phys.attached_body.EntityBoundingBoxBody
import cn.solarmoon.spark_core.api.phys.attached_body.putBody
import cn.solarmoon.spark_core.api.phys.thread.getPhysWorld
import cn.solarmoon.spark_core.registry.common.SparkAttachments
import cn.solarmoon.spark_core.registry.common.SparkSkills
import net.minecraft.world.entity.animal.IronGolem
import net.minecraft.world.entity.player.Player
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.attachment.IAttachmentHolder
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent
import net.neoforged.neoforge.event.tick.EntityTickEvent
import org.ode4j.math.DVector3

class AttackedDataController {

    @SubscribeEvent
    private fun entityTick(event: EntityTickEvent.Pre) {
        val entity = event.entity
        val level = entity.level()

        if (entity is IEntityAnimatable<*>) {
            SparkSkills.PLAYER_SWORD_COMBO_0.value().tick(entity)
        }

        if (entity is IronGolem) {

        }
    }

    @SubscribeEvent
    private fun hurt(event: LivingDamageEvent.Post) {
        val entity = event.entity
        SparkCore.LOGGER.info(entity.getAttackedData()?.damagedBody?.name + " " + entity.getAttackedData().toString())
    }

    @SubscribeEvent
    private fun join(event: EntityJoinLevelEvent) {
        val entity = event.entity
        val level = event.level
        if (entity is IEntityAnimatable<*>) {
            entity.animData.model.bones.forEach {
                val body = AnimatedCubeBody(it.name, level, entity)
                entity.putBody(body)
            }
            if (entity is Player) {
                val body = EntityAnimatedAttackBody("attack", "rightItem", level, entity).apply {
                    size = DVector3(0.5, 0.5, 1.0)
                    offset = DVector3(0.0, 0.0, -0.5)
                }
                entity.putBody(body)
            }
        } else {
            val body = EntityBoundingBoxBody(level, entity)
            entity.putBody(body)
        }
    }

    @SubscribeEvent
    private fun leave(event: EntityLeaveLevelEvent) {
        val entity = event.entity
        val level = event.level
        entity.getData(SparkAttachments.BODY).values.forEach {
            level.getPhysWorld().laterConsume { it.body.destroy() }
        }
    }

}