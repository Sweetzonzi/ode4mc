package cn.solarmoon.spark_core.api.entity.attack

import cn.solarmoon.spark_core.api.phys.DxEntity
import cn.solarmoon.spark_core.registry.common.SparkEntityTypes
import cn.solarmoon.spark_core.registry.common.SparkSkills
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.animal.IronGolem
import net.minecraft.world.entity.player.Player
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent
import net.neoforged.neoforge.event.tick.EntityTickEvent

class AttackedDataController {

    @SubscribeEvent
    private fun entityTick(event: EntityTickEvent.Pre) {
        val entity = event.entity
        val level = entity.level()

        if (entity is Player) {
            SparkSkills.PLAYER_SWORD_COMBO_0.value().tick(entity)
        }

        if (entity is IronGolem) {

        }
    }

    @SubscribeEvent
    private fun join(event: EntityJoinLevelEvent) {
        val entity = event.entity
        val level = event.level
        if (level is ServerLevel && entity !is DxEntity) {
            SparkEntityTypes.DX_BOUNDING_BOX.value().create(level)?.let {
                level.addFreshEntity(it.apply { setEntityOwner(entity) })
            }
            if (entity is Player) {
                SparkEntityTypes.DX_ANIM_ATTACK.value().create(level)?.let {
                    level.addFreshEntity(it.apply { setEntityOwner(entity); bodyName = "rightItem" })
                }
            }
        }
    }

}