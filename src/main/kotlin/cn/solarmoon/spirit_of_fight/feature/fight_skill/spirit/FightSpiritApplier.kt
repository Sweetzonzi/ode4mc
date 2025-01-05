package cn.solarmoon.spirit_of_fight.feature.fight_skill.spirit

import cn.solarmoon.spirit_of_fight.feature.fight_skill.sync.FightSpiritPayload
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.EntityTickEvent

object FightSpiritApplier {

    @SubscribeEvent
    private fun entityTick(event: EntityTickEvent.Post) { // 战意作为结算性质的机制需要在结尾tick而非开头，否则可能会比攻击先触发导致下一个tick才会进行当前tick攻击后应有的结果
        val entity = event.entity
        val level = entity.level()
        val fs = entity.getFightSpirit()
        if (!level.isClientSide) {
            fs.syncToClient(entity.id, FightSpiritPayload.Type.SYNC)
        }
        fs.tick()
    }

}