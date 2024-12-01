package cn.solarmoon.spirit_of_fight.feature.fight_skill.attack

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.event.EntityTurnEvent
import cn.solarmoon.spark_core.api.event.PlayerRenderAnimInFirstPersonEvent
import cn.solarmoon.spirit_of_fight.feature.fight_skill.IFightSkillHolder
import cn.solarmoon.spirit_of_fight.feature.hit.HitType
import cn.solarmoon.spirit_of_fight.feature.lock_on.LockOnController
import net.minecraft.client.player.LocalPlayer
import net.minecraft.util.Mth
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.client.event.ViewportEvent
import org.joml.Vector2f

class CameraAdjuster {

    companion object {
        @JvmStatic
        val CAMERA_TURN: Vector2f = Vector2f()
    }

    @SubscribeEvent
    private fun lockHeadTurn(event: EntityTurnEvent) {
        val entity = event.entity
        val xRot = event.xRot.toFloat()
        val yRot = event.yRot.toFloat()
        if (entity is IFightSkillHolder && entity is IEntityAnimatable<*>) {
            val skill = entity.skillController
            if (
                skill != null &&
                // 0tick可以转，这样可以在切换动作时变向不至于连招时方向一定定死了
                ((skill.isAttacking { !it.isInTransition } && skill.getPlayingSkillAnim{ !it.isCancelled }!!.tick != 0.0) || skill.guard.isBacking { !it.isInTransition })
                || HitType.isPlayingHitAnim(entity) { !it.isCancelled }
                ) {
                if (entity is LocalPlayer && !LockOnController.hasTarget) CAMERA_TURN.add(xRot, yRot)
                event.isCanceled = true
            } else if (CAMERA_TURN != Vector2f()) {
                val x = CAMERA_TURN.x.toDouble()
                val y = CAMERA_TURN.y.toDouble()
                CAMERA_TURN.set(0f)
                entity.turn(y, x)
            }
        }
    }

    @SubscribeEvent
    private fun offsetCameraWhenLock(event: ViewportEvent.ComputeCameraAngles) {
        if (CAMERA_TURN != Vector2f()) {
            val f = CAMERA_TURN.x * 0.15f
            val f1 = CAMERA_TURN.y * 0.15f
            val entity = event.camera.entity
            event.yaw = entity.yRot + f1
            event.pitch = Mth.clamp(entity.xRot + f, -90f, 90f)
        }
    }

    @SubscribeEvent
    private fun renderAnimInFirstPersonWhenAttack(event: PlayerRenderAnimInFirstPersonEvent) {
        val player = event.player
        if (player !is IFightSkillHolder) return
        val sc = player.skillController ?: return
        event.shouldRender = sc.isAttacking() || sc.guard.isPlaying()
    }

}