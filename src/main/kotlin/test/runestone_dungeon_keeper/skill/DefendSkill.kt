package test.runestone_dungeon_keeper.skill

import cn.solarmoon.spark_core.api.animation.anim.play.AnimData
import cn.solarmoon.spark_core.api.entity.ai.skill.Skill
import cn.solarmoon.spark_core.api.util.EntityUtil
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import test.runestone_dungeon_keeper.Tiasis

class DefendSkill(entity: Tiasis, maxCooldown: Int): Skill<Tiasis>(entity, maxCooldown) {

    override val condition: Boolean
        get() {
            val level = entity.level()
            if (level is ServerLevel && EntityUtil.canSee(entity, entity.target, 45.0)) {
                return true
            }
            return false
        }

    override fun tick() {
//        val level = entity.level()
//        if (entity.animController.isPlaying("attack3") && AnimData.tick != 0) {
//            val sp = entity.getBonePivot("shield2")
//            level.addParticle(ParticleTypes.CRIT, true, sp.x.toDouble(), sp.y.toDouble(), sp.z.toDouble(), 0.0, 0.0, 0.0)
//        }
    }

    override fun onStart() {
//        val level = entity.level()
//        entity.animController.start("attack3", transTime = 0, lifeTime = 0.5)
//        level.playSound(null, entity.onPos.above(), SoundEvents.SHIELD_BLOCK, SoundSource.VOICE)
    }

}