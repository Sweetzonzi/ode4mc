package cn.solarmoon.spark_core.api.entity.skill

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.event.EntityGetWeaponEvent
import cn.solarmoon.spark_core.api.event.PlayerGetAttackStrengthEvent
import cn.solarmoon.spirit_of_fight.feature.fight_skill.IFightSkillHolder
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent
import net.neoforged.neoforge.event.entity.player.SweepAttackEvent
import net.neoforged.neoforge.event.tick.EntityTickEvent

class AnimSkillApplier {

    @SubscribeEvent
    private fun whenHurt(event: LivingIncomingDamageEvent) {
        val entity = event.entity
        val source = event.source
        val amount = event.originalAmount
        if (entity is IAnimSkillHolder<*>) {
            val skillManager = entity.skillController
            if (skillManager != null) {
                for (skill in skillManager.skillGroup) {
                    val anim = skill.getPlayingAnim() ?: continue
                    if (!skill.whenAttackedInAnim(source, amount, anim)) {
                        event.isCanceled = true
                        break
                    }
                }
            }
        }
    }

    @SubscribeEvent
    private fun entityTick(event: EntityTickEvent.Pre) {
        val entity = event.entity
        if (entity is IAnimSkillHolder<*> && entity is IEntityAnimatable<*>) {
            val skillController = entity.skillController
            val switch = entity.persistentData.getBoolean("SkillSwitch")

            entity.getAllSkills().forEach { it.tick() }

            if (skillController != null) {
                if (!switch) entity.persistentData.putBoolean("SkillSwitch", true)
            } else {
                // 一旦战技从有到无则刷新所有动作
                if (switch) {
                    entity.persistentData.putBoolean("SkillSwitch", false)
                    entity.animController.stopAllAnimation()
                }
            }
        }
    }

    /**
     * 当攻击以生物手中武器为媒介时，并且是比如双刀流的情况，根据[AnimSkill.getAttackItem]具体设定生物在某个动作中使用左手或是右手或是任意武器进行攻击
     */
    @SubscribeEvent
    private fun modifyWeapon(event: EntityGetWeaponEvent) {
        val entity = event.entity
        if (entity is IAnimSkillHolder<*>) {
            val skillController = entity.skillController ?: return
            if (skillController.isAttacking()) {
                val skill = skillController.getPlayingSkill<AnimSkill>() ?: return
                val anim = skill.getPlayingAnim() ?: return
                event.weapon = skill.getAttackItem(event.weapon, anim)
            }
        }
    }

    /**
     * 保证[net.minecraft.world.entity.player.Player.attack]攻击伤害不随冷却衰减，这在以动作为驱动的攻击中是合理的，因为动作本身的运动过程就是冷却
     */
    @SubscribeEvent
    private fun modifyPlayerAttackAttenuate(event: PlayerGetAttackStrengthEvent) {
        val player = event.entity
        if (player !is IAnimSkillHolder<*>) return
        val skill = player.skillController ?: return
        if (skill.isAttacking()) {
            event.attackStrengthScale = 1f
        }
    }

    @SubscribeEvent
    private fun modifyAttackDamageScale(event: LivingIncomingDamageEvent) {
        val entity = event.source.entity // 使用根伤害源，因为这里的倍率根本上而言是由玩家动作驱动的，因此比如在射箭时，此动作可能力度稍大从而增加了伤害倍率，如果设在直接伤害源（箭）上显然不符合这里以动作为核心的情境
        if (entity !is IAnimSkillHolder<*>) return
        val skillController = entity.skillController ?: return
        if (skillController.isAttacking()) {
            val skill = skillController.getPlayingSkill<AnimSkill>() ?: return
            val anim = skill.getPlayingAnim() ?: return
            skill.getAttackDamageMultiplier(anim)?.let {
                event.amount = it * event.originalAmount
            }
        }
    }

}