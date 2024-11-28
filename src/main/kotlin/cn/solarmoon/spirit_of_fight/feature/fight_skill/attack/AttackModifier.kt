package cn.solarmoon.spirit_of_fight.feature.fight_skill.attack

import cn.solarmoon.spark_core.api.entity.skill.IAnimSkillHolder
import cn.solarmoon.spark_core.api.event.EntityGetWeaponEvent
import cn.solarmoon.spark_core.api.event.PlayerGetAttackStrengthEvent
import cn.solarmoon.spirit_of_fight.feature.fight_skill.IFightSkillHolder
import cn.solarmoon.spirit_of_fight.feature.fight_skill.spirit.getFightSpirit
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent
import net.neoforged.neoforge.event.entity.player.SweepAttackEvent
import net.neoforged.neoforge.event.tick.EntityTickEvent

class AttackModifier {

    /**
     * 原版横扫从此再见
     */
    @SubscribeEvent
    private fun playerSweep(event: SweepAttackEvent) {
        val player = event.entity
        if (player !is IFightSkillHolder) return
        val skill = player.skillController ?: return
        if (skill.isAttacking()) {
            event.isSweeping = false
        }
    }

    /**
     * 使能够兼容别的模组的暴击修改，并且把原版跳劈删去
     */
    @SubscribeEvent
    private fun playerCriticalHit(event: CriticalHitEvent) {
        val player = event.entity
        if (player !is IFightSkillHolder) return
        val skill = player.skillController ?: return
        if (skill.isAttacking()) {
            // 逻辑是原版暴击只能在跳劈情况下触发，因此直接删掉原版跳劈，但是别的模组由暴击率驱动的概率性伤害显然理应不受其影响
            if (event.vanillaMultiplier == 1.5f) event.isCriticalHit = false
        }
    }

    /**
     * 取消默认情况下击退的y轴向量
     */
    @SubscribeEvent
    private fun knockBackModify(event: LivingKnockBackEvent) {
        val entity = event.entity
        entity.setOnGround(false)
    }

}